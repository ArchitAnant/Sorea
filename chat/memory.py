import json
import asyncio
from typing import List, Dict, Any, Optional
from datetime import datetime, timedelta
from models import ConversationMemory, ChatPair, UserProfile
from config import config
from firebase_manager import firebase_manager


class SimpleMessage:
    """Simple message class for backward compatibility."""
    def __init__(self, role: str, content: str, timestamp: datetime = None, 
                 emotion_detected: str = None, urgency_level: int = 1):
        self.role = role
        self.content = content
        
        # Ensure timestamp is timezone-aware
        if timestamp is None:
            from datetime import timezone
            self.timestamp = datetime.now(timezone.utc)
        elif hasattr(timestamp, 'tzinfo') and timestamp.tzinfo is None:
            # Make timezone-naive timestamp timezone-aware (UTC)
            from datetime import timezone
            self.timestamp = timestamp.replace(tzinfo=timezone.utc)
        else:
            self.timestamp = timestamp
            
        self.emotion_detected = emotion_detected
        self.urgency_level = urgency_level


class MemoryManager:
    """Manages conversation memory, user profiles, and chat history using Firebase."""
    
    def __init__(self):
        # Firebase-first approach - no local storage
        self.conversations: Dict[str, ConversationMemory] = {}
        self.user_profiles: Dict[str, UserProfile] = {}
    
    def create_user_profile(self, email: str, name: str = None) -> UserProfile:
        """Create a new user profile."""
        # Create in Firebase only
        profile = firebase_manager.create_user_profile(email, name, name)
        
        # Cache locally for performance
        self.user_profiles[email] = profile
        return profile
    
    def get_user_profile(self, email: str) -> UserProfile:
        """Get user profile from Firebase with correct lastInteraction."""
        # Get from the standard location first
        profile = firebase_manager.get_user_profile(email)
        
        # Try to get lastInteraction from profiles table
        try:
            # Note: No longer tracking lastInteraction - removed for simplification
            pass
        except Exception as e:
            # Silently handle any errors - not critical for functionality
            pass
        
        # Cache locally for performance
        self.user_profiles[email] = profile
        return profile
    
    def get_all_user_profiles(self) -> Dict[str, UserProfile]:
        """Get all user profiles from Firebase."""
        return firebase_manager.get_all_user_profiles()
    
    def find_user_by_name(self, name: str) -> UserProfile:
        """Find user profile by name (case insensitive)."""
        return firebase_manager.find_user_by_name(name)
    
    def update_user_profile(self, email: str, updates: Dict[str, Any]):
        """Update user profile information."""
        # Update in Firebase only
        firebase_manager.update_user_profile(email, updates)
        
        # Update local cache if exists
        if email in self.user_profiles:
            profile = self.user_profiles[email]
            for key, value in updates.items():
                if hasattr(profile, key):
                    setattr(profile, key, value)
    
    def create_conversation(self, email: str) -> ConversationMemory:
        """Create a new conversation memory."""
        conversation_id = f"{email}_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        conversation = ConversationMemory(
            conversation_id=conversation_id
        )
        self.conversations[conversation_id] = conversation
        return conversation
    
    def add_message(self, email: str, role: str, content: str, 
                   emotion_detected: str = None, urgency_level: int = 1) -> None:
        """Add a message to the conversation using chat pairs."""
        # Store the message temporarily for chat pair formation
        if not hasattr(self, '_temp_user_message'):
            self._temp_user_message = {}
        
        if role == "user":
            # Store user message temporarily
            self._temp_user_message[email] = {
                'content': content,
                'emotion_detected': emotion_detected,
                'urgency_level': urgency_level
            }
        elif role == "assistant" and email in self._temp_user_message:
            # Create chat pair when we have both user and assistant messages
            user_msg = self._temp_user_message[email]
            firebase_manager.add_chat_pair(
                email=email,
                user_message=user_msg['content'],
                model_response=content,
                emotion_detected=user_msg.get('emotion_detected'),
                urgency_level=user_msg.get('urgency_level', 1)
            )
            # Clear temporary storage
            del self._temp_user_message[email]
        else:
            # Fallback: just add to Firebase using deprecated method
            firebase_manager.add_message(email, role, content, emotion_detected, urgency_level)
    
    def get_current_conversation(self, email: str) -> ConversationMemory:
        """Get the most recent conversation for a user."""
        # Since we simplified ConversationMemory, create a simple one if needed
        conversation_id = f"conv_{email}_{datetime.now().strftime('%Y%m%d')}"
        if conversation_id not in self.conversations:
            self.conversations[conversation_id] = ConversationMemory(
                conversation_id=conversation_id
            )
        return self.conversations[conversation_id]
    
    def get_recent_messages(self, email: str, limit: int = 10) -> List[SimpleMessage]:
        """Get recent messages for context from Firebase using chat pairs."""
        # Get recent chat pairs from Firebase
        chat_pairs = firebase_manager.get_recent_chat(email, limit // 2)  # Each pair has 2 messages
        
        # Convert chat pairs to SimpleMessage objects for backward compatibility
        messages = []
        for chat_pair in chat_pairs:
            try:
                # Ensure timestamp is timezone-aware
                timestamp = chat_pair.timestamp
                if timestamp and hasattr(timestamp, 'replace') and timestamp.tzinfo is None:
                    # Make timezone-naive timestamps timezone-aware (UTC)
                    from datetime import timezone
                    timestamp = timestamp.replace(tzinfo=timezone.utc)
                elif not timestamp:
                    # If no timestamp, use current time as timezone-aware
                    from datetime import timezone
                    timestamp = datetime.now(timezone.utc)
                
                # Add user message
                user_msg = SimpleMessage(
                    role="user",
                    content=chat_pair.user,
                    timestamp=timestamp,
                    emotion_detected=chat_pair.emotion_detected,
                    urgency_level=chat_pair.urgency_level
                )
                messages.append(user_msg)
                
                # Add model response
                model_msg = SimpleMessage(
                    role="assistant",
                    content=chat_pair.model,
                    timestamp=timestamp
                )
                messages.append(model_msg)
                
            except Exception as e:
                # Skip invalid pairs silently
                continue
        
        # Sort by timestamp and return most recent
        messages.sort(key=lambda x: x.timestamp, reverse=True)
        return messages[:limit]
    
    def summarize_conversation(self, conversation_id: str):
        """Create a summary of the conversation to manage memory."""
        if conversation_id not in self.conversations:
            return
        
        conv = self.conversations[conversation_id]
        
        # Simple summarization logic (can be enhanced with LLM)
        moods = []
        key_points = []
        
        for message in conv.messages:
            if message.role == "user":
                # Extract key information
                if message.emotion_detected:
                    moods.append(message.emotion_detected)
        
        # Create summary based on emotions and basic message count
        conv.key_topics = []  # Remove keyword-based topic extraction
        conv.summary = f"Conversation with {len(conv.messages)} messages. User moods: {', '.join(set(moods[-5:]))}"
        
        # Keep only recent messages and summary
        if len(conv.messages) > config.max_conversation_history:
            conv.messages = conv.messages[-config.max_conversation_history//2:]
        
        self.save_memory()
    
    def get_conversation_context(self, email: str) -> str:
        """Get formatted conversation context for the LLM including temporal awareness."""
        profile = self.get_user_profile(email)
        recent_messages = self.get_recent_messages(email, 5)
        current_conv = self.get_current_conversation(email)
        
        # Use timezone-aware datetime to match Firebase timestamps
        from datetime import timezone
        now = datetime.now(timezone.utc)
        
        # Start with user profile and current context
        context = f"User Profile: {profile.name or 'friend'}"
        
        # Add current date/time context for the AI
        context += f"\n📅 Current date/time: {now.strftime('%A, %B %d, %Y at %I:%M %p')}"
        
        # Check if this is the first chat of the day and include summary
        if self._is_first_chat_of_day(email):
            last_summary = self._get_last_conversation_summary(email)
            if last_summary:
                summary_date = last_summary.get('date', 'unknown')
                summary_text = last_summary.get('summary_text', '')
                context += f"\n📋 Last conversation summary ({summary_date}): {summary_text}"
        
        # Removed mental_health_concerns since UserProfile no longer has this field
        
        if current_conv and current_conv.summary:
            context += f"\nConversation Summary: {current_conv.summary}"
        
        if current_conv and current_conv.key_topics:
            context += f"\nPrevious topics: {', '.join(current_conv.key_topics[:5])}"
        
        if recent_messages:
            context += "\nRecent conversation with timestamps:\n"
            for msg in recent_messages[-5:]:  # Show more recent messages with time
                time_ago = self._format_time_ago(msg.timestamp, now)
                context += f"{msg.role} ({time_ago}): {msg.content[:100]}...\n"
        
        return context
    
    def _format_time_ago(self, timestamp: datetime, current_time: datetime) -> str:
        """Format how long ago a message was sent."""
        time_diff = current_time - timestamp
        
        if time_diff.days > 0:
            if time_diff.days == 1:
                return "yesterday"
            else:
                return f"{time_diff.days} days ago"
        elif time_diff.seconds > 3600:
            hours = time_diff.seconds // 3600
            return f"{hours}h ago"
        elif time_diff.seconds > 60:
            minutes = time_diff.seconds // 60
            return f"{minutes}m ago"
        else:
            return "just now"

    def _is_first_chat_of_day(self, email: str) -> bool:
        """Check if this is the first chat of the current day."""
        try:
            # Get today's conversation data
            today_str = datetime.now().strftime('%Y%m%d')
            today_conv = firebase_manager.get_conversation_by_date(email, today_str)
            
            # If no messages exist for today, this is the first chat
            if not today_conv or not today_conv.get('messages'):
                return True
                
            # If messages exist, this is not the first chat
            return False
            
        except Exception:
            # If we can't determine, assume it's not the first chat to be safe
            return False

    def _get_last_conversation_summary(self, email: str) -> Optional[dict]:
        """Get the summary of user's last conversation day if it exists."""
        # Find the last day user actually had conversations (not just when they were online)
        last_conversation_date = firebase_manager.get_last_conversation_date(email)
        
        if last_conversation_date:
            today_date = datetime.now().date()
            
            # Only get summary if it's not today (today's conversation is ongoing)
            if last_conversation_date != today_date:
                date_str = last_conversation_date.strftime('%Y%m%d')
                return firebase_manager.get_daily_summary(email, date_str)
        
        return None

# Global memory manager instance
memory_manager = MemoryManager()
