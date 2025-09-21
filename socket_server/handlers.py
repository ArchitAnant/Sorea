import json
import logging
from datetime import datetime, UTC
import websockets
from auth import verify_firebase_token
from rooms import register, unregister, broadcast
from db import save_message_to_db, fetch_messages_keyset
import asyncio

async def chat_handler(websocket):
    try:
        join_message = await websocket.recv()
        data = json.loads(join_message)

        if data.get("type") != "join":
            await websocket.close(1008, "First message must be join.")
            return

        token, roomId = data.get("token"), data.get("roomId")
        if not token or not roomId:
            await websocket.close(1008, "Token & roomId required.")
            return

        decoded_token = await verify_firebase_token(token)
        if not decoded_token:
            await websocket.close(4001, "Invalid authentication token.")
            return

        trusted_userId = decoded_token['user_id']
        await register(websocket, roomId, trusted_userId)

        async for message in websocket:
            await handle_message(websocket, message)

    except websockets.exceptions.ConnectionClosed as e:
        logging.info(f"Connection closed: {e.code} {e.reason}")
    finally:
        await unregister(websocket)


async def handle_message(websocket, raw_message):
    try:
        data = json.loads(raw_message)
    except json.JSONDecodeError:
        logging.warning("Invalid JSON")
        return

    if data.get("type") == "message":
        payload = {
            "type": "message",
            "text": data.get("text"),
            "userId": websocket.user_id,
            "roomId": websocket.room_id,
            "timestamp": datetime.now(UTC).isoformat()
        }
        await broadcast(websocket.room_id, json.dumps(payload), exclude_sender=True, sender_websocket=websocket)
        await save_message_to_db(websocket.room_id, websocket.user_id, data.get("text"))

    elif data.get("type") == "join":
        new_roomId = data.get("roomId")
        current_userId = websocket.user_id
                
        if new_roomId and new_roomId != websocket.room_id:
            logging.info(f"User '{current_userId}' is switching to room '{new_roomId}'.")
            await unregister(websocket)
            await register(websocket, new_roomId, current_userId)
        else:
            logging.warning(f"User '{current_userId}' sent an invalid room-switch request.")


    elif data.get("type") == "load_chat":
        before = data.get("before")  # ISO timestamp string
        limit = data.get("limit", 50)
        try:
            limit = int(limit)
            if limit <= 0 or limit > 100:
                limit = 50
        except (ValueError, TypeError):
            limit = 50

        messages = fetch_messages_keyset(websocket.room_id, before, limit)
        response = {
            "type": "chat_history",
            "messages": messages
        }
        await websocket.send(json.dumps(response))
        

    else:
        logging.warning(f"Unknown message type: {data.get('type')}")
