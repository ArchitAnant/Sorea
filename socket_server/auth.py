import logging
import asyncio
from google.oauth2 import id_token
from google.auth.transport import requests
from config import FIREBASE_PROJECT_ID

def _verify_firebase_token_blocking(token):
    try:
        return id_token.verify_firebase_token(token, requests.Request(), audience=FIREBASE_PROJECT_ID)
    except ValueError as e:
        logging.warning(f"Token verification failed: {e}")
        return None

async def verify_firebase_token(token):
    return await asyncio.to_thread(_verify_firebase_token_blocking, token)
