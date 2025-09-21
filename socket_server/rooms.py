import logging
import json
import asyncio

ROOMS = {}

async def register(websocket, roomId, userId):
    if roomId not in ROOMS:
        ROOMS[roomId] = set()
    ROOMS[roomId].add(websocket)
    websocket.room_id = roomId
    websocket.user_id = userId
    logging.info(f"User '{userId}' joined room '{roomId}'")
    announcement = {"type": "announcement", "message": f"User '{userId}' joined the room."}
    await broadcast(roomId, json.dumps(announcement), exclude_sender=False)

async def unregister(websocket):
    roomId = getattr(websocket, 'room_id', None)
    userId = getattr(websocket, 'user_id', 'Unknown')
    if roomId and roomId in ROOMS and websocket in ROOMS[roomId]:
        ROOMS[roomId].remove(websocket)
        logging.info(f"User '{userId}' left room '{roomId}'")
        announcement = {"type": "announcement", "message": f"User '{userId}' left the room."}
        await broadcast(roomId, json.dumps(announcement), exclude_sender=False)
        if not ROOMS[roomId]:
            del ROOMS[roomId]
            logging.info(f"Room '{roomId}' deleted (empty).")

async def broadcast(roomId, message, exclude_sender=True, sender_websocket=None):
    if roomId in ROOMS:
        clients = list(ROOMS[roomId])
        tasks = [client.send(message) for client in clients if not (exclude_sender and client == sender_websocket)]
        if tasks:
            await asyncio.gather(*tasks)
