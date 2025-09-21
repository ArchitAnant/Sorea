import asyncio
import logging
import websockets
import signal
from config import DATABASE_URL, SERVER_HOST, SERVER_PORT
from db import init_db_pool, test_db_connection
from handlers import chat_handler
import os


async def health_check(path, request_headers):
    """Handle health check requests from Render and other monitoring services"""
    if path == "/health":
        logging.info("Health check requested")
        
        return (200, [("Content-Type", "text/plain")], b"OK")
    
   
    return None

async def shutdown(server):
    logging.info("Shutdown sequence started...")
    server.close()
    await server.wait_closed()

async def main():
    logging.basicConfig(level=logging.INFO)
    logging.info("Starting server...")
    db_pool = init_db_pool(DATABASE_URL)
    logging.info("Database pool initialized.")
    if not test_db_connection():
        logging.error("Database connection test failed. Exiting.")
        return
    logging.info("Database connection test succeeded.")
    loop = asyncio.get_running_loop()
    stop = loop.create_future()
    logging.info("Setting up signal handlers...")
    if os.name != 'nt':
        loop.add_signal_handler(signal.SIGINT, stop.set_result, None)
        loop.add_signal_handler(signal.SIGTERM, stop.set_result, None)
        
    logging.info("Starting WebSocket server...")
    server = await websockets.serve(
        chat_handler,
        SERVER_HOST,
        SERVER_PORT,
        process_request=health_check  
    )
    #server = await websockets.serve(chat_handler, SERVER_HOST, SERVER_PORT)
    logging.info(f"Server running on ws://{SERVER_HOST}:{SERVER_PORT}")
    await stop
    await shutdown(server)

if __name__ == "__main__":
    asyncio.run(main())
