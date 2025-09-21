import random
from typing import List
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import HumanMessage, SystemMessage
from models import MentalHealthTopicFilter, ChatResponse
from config import config


class MentalHealthFilter:
    """Filter to ensure conversations stay focused on mental health topics."""
    
    def __init__(self):
        self.llm = ChatGoogleGenerativeAI(
            model=config.model_name,
            google_api_key=config.gemini_api_key,
            temperature=0.3  # Lower temperature for more consistent filtering
        )
    
    def is_mental_health_related(self, message: str) -> MentalHealthTopicFilter:
        """Determine if a message is related to mental health using Gemini API."""
        
        system_prompt = """You are a mental health topic classifier for a therapeutic chatbot named MyBro. Your job is to determine if ANY message could be part of a valid mental health conversation or therapeutic relationship.

        MENTAL HEALTH RELATED includes:
        - Emotions and feelings (sad, happy, anxious, stressed, angry, excited, etc.)
        - Mental health conditions and symptoms
        - Life challenges, struggles, and personal issues
        - Relationships, family, and social problems  
        - Work stress, school pressure, life changes
        - Sleep, self-care, and wellness topics
        - Personal growth, therapy, and healing
        - Greetings and check-ins ("Hi", "Hello", "How are you?") - these are valid starts to mental health conversations
        - Conversation continuity ("Do you remember me?", "We talked before", "Last time...")
        - Any personal questions that could lead to emotional support
        - Casual conversation that builds therapeutic rapport
        - Questions about the AI's memory or previous interactions

        BE VERY INCLUSIVE - Mental health support often starts with simple interactions like:
        - "Hi" - someone reaching out for connection
        - "How's it going?" - checking in on well-being  
        - "Remember me?" - maintaining therapeutic relationship
        - "What's up?" - casual opening to deeper conversation

        ONLY classify as NON-mental health if it's clearly:
        - Pure academic/factual questions with no personal element ("What's the capital of France?")
        - Technical instructions unrelated to well-being ("How do I code in Python?")
        - Commercial/business inquiries
        - Spam or completely unrelated content

        IMPORTANT: When in doubt, classify as MENTAL HEALTH RELATED. It's better to be inclusive than to block someone seeking support.

        Respond with only 'YES' or 'NO' and a brief reason (max 20 words)."""
                
        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Is this mental health related: '{message}'")
            ]
            
            response = self.llm.invoke(messages)
            response_text = response.content.strip().upper()
            
            is_related = response_text.startswith('YES')
            confidence = 0.7 if is_related else 0.3
            
            return MentalHealthTopicFilter(
                is_mental_health_related=is_related,
                confidence_score=confidence,
                detected_topics=[],
                reason=response.content
            )
            
        except Exception as e:
            # Fallback to conservative approach
            return MentalHealthTopicFilter(
                is_mental_health_related=False,
                confidence_score=0.1,
                detected_topics=[],
                reason="Error in classification, defaulting to non-mental health"
            )
    
    def detect_emotion(self, message: str) -> tuple[str, int]:
        """Detect primary emotion and urgency level (1-5) using LLM."""
        
        system_prompt = """You are an expert emotion and urgency detector for a mental health chatbot. Analyze the user's message and respond with EXACTLY this format:

        EMOTION: [one word emotion]
        URGENCY: [number 1-5]

        EMOTION OPTIONS (pick the most accurate):
        - anxious (worried, nervous, fearful, panic)
        - depressed (sad, hopeless, empty, worthless, down)
        - angry (frustrated, mad, furious, irritated, upset)
        - happy (joyful, excited, cheerful, positive, content)
        - stressed (overwhelmed, pressured, tense, burned out)
        - lonely (isolated, disconnected, alone, abandoned)
        - confused (lost, uncertain, unclear, bewildered)
        - grateful (thankful, appreciative, blessed)
        - neutral (calm, balanced, no strong emotion)

        URGENCY LEVELS:
        1 = Casual conversation, no distress
        2 = Mild concern or discomfort
        3 = Moderate distress, needs support
        4 = High distress, significant concern
        5 = CRISIS - immediate danger, suicidal thoughts with plans

        IMPORTANT: Only use urgency 5 for immediate danger with specific plans like "I'm going to kill myself tonight" or "I have pills ready to end it". General sadness or "want to die" thoughts are urgency 2-3, not 5.

        Respond with ONLY the two lines in the exact format shown above."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"User message: {message}")
            ]
            
            response = self.llm.invoke(messages)
            response_text = response.content.strip()
            
            # Parse the response
            lines = response_text.split('\n')
            emotion = "neutral"
            urgency_level = 1
            
            for line in lines:
                if line.startswith("EMOTION:"):
                    emotion = line.split(":", 1)[1].strip().lower()
                elif line.startswith("URGENCY:"):
                    try:
                        urgency_level = int(line.split(":", 1)[1].strip())
                        urgency_level = max(1, min(5, urgency_level))  # Clamp between 1-5
                    except ValueError:
                        urgency_level = 1
            
            return emotion, urgency_level
            
        except Exception as e:
            pass