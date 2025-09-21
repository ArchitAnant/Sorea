import azure.functions as func
import logging
import json
from datetime import datetime, timezone
import asyncio

from main import android_chat


app = func.FunctionApp()


CORS_HEADERS = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, Authorization, Accept",
}


@app.route(route="chat", auth_level=func.AuthLevel.FUNCTION)
def chat_handler(req: func.HttpRequest) -> func.HttpResponse:
    """
    Handles POST requests to process a user's message via the chatbot.
    """
    logging.info('Chat handler function processed a request.')

    
    if req.method == "OPTIONS":
        return func.HttpResponse("", status_code=204, headers=CORS_HEADERS)

    try:
        try:
            req_body = req.get_json()
            email = req_body.get('email')
            message = req_body.get('message')
        except ValueError:
            return func.HttpResponse(
                json.dumps({"error": "Invalid JSON format."}),
                status_code=400, mimetype="application/json", headers=CORS_HEADERS
            )

        if not email or not message:
            return func.HttpResponse(
                json.dumps({"error": "Please provide 'email' and 'message'."}),
                status_code=400, mimetype="application/json", headers=CORS_HEADERS
            )

        
        
        chat_response = android_chat(user_prompt=message, user_email=email)


        response_data = {
            "message": chat_response,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }

        return func.HttpResponse(
            json.dumps(response_data),
            mimetype="application/json",
            status_code=200,
            headers=CORS_HEADERS
        )

    except Exception as e:
        logging.error(f"An error occurred in chat_handler: {e}", exc_info=True)
        return func.HttpResponse(
            json.dumps({"error": "An internal server error occurred."}),
            status_code=500, mimetype="application/json", headers=CORS_HEADERS
        )