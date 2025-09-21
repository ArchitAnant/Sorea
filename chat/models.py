from typing import List, Optional, Dict, Any
from datetime import datetime, date
from pydantic import BaseModel, Field


class ImportantEvent(BaseModel):
    """Tracks important upcoming events mentioned in conversation."""
    eventType: str  # 'exam', 'interview', 'appointment'
    description: str  # What the user said about it
    eventDate: Optional[str] = None  # When it's happening (stored as string)
    mentionedAt: str = Field(default_factory=lambda: datetime.now().isoformat())  
    followUpNeeded: bool = True
    followUpDone: bool = False


class UserProfile(BaseModel):
    """User profile information for personalization."""
    name: Optional[str] = None


class ChatPair(BaseModel):
    """One chat pair containing both user message and model response."""
    user: str  # User's message
    model: str  # Assistant/LLM response
    timestamp: datetime = Field(default_factory=datetime.now)
    emotion_detected: Optional[str] = None  # Emotion detected in user's message
    urgency_level: Optional[int] = Field(default=1, ge=1, le=5)  # Urgency of user's message


class ConversationMemory(BaseModel):
    """Memory structure for conversation history."""
    conversation_id: str
    chat: List[ChatPair] = Field(default_factory=list)
    summary: str = ""
    key_topics: List[str] = Field(default_factory=list)
    user_mood_trend: List[Dict[str, Any]] = Field(default_factory=list)
    important_details: Dict[str, Any] = Field(default_factory=dict)


class ChatResponse(BaseModel):
    """Response from the chatbot."""
    message: str
    emotion_tone: str
    suggestions: List[str] = Field(default_factory=list)
    follow_up_questions: List[str] = Field(default_factory=list)
    urgency_detected: bool = False
    professional_help_suggested: bool = False


class MentalHealthTopicFilter(BaseModel):
    """Filter for mental health related topics."""
    is_mental_health_related: bool
    confidence_score: float = Field(ge=0.0, le=1.0)
    detected_topics: List[str] = Field(default_factory=list)
    reason: str = ""
