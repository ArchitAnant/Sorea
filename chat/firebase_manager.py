
"""
Firebase Manager for Email-Based User Schema
No sessions, no analytics, no separate tables - just users organized by email
"""

import os
import secrets
from datetime import datetime, date
from typing import List, Dict, Any, Optional
import firebase_admin
from firebase_admin import credentials, firestore
from google.cloud.firestore import FieldFilter
from models import UserProfile, ChatPair, ImportantEvent

# Load environment variables
from dotenv import load_dotenv
load_dotenv()

class FirebaseManager:
    """Firebase manager with email-based user organization using Firestore."""
    
    def __init__(self):
        self.db = None
        self.initialize_firebase()
    
    def initialize_firebase(self):
        """Initialize Firebase using environment variables or service account file."""
        try:
            if not firebase_admin._apps:
                # Try environment variables first
                if self._use_env_credentials():
                    print("SUCCESS: Firebase initialized with environment variables!")
                # Fallback to service account file
                elif self._use_service_account_file():
                    print("SUCCESS: Firebase initialized with service account file!")
                else:
                    raise Exception("No valid Firebase credentials found")
            
            self.db = firestore.client()
            
        except Exception as e:
            print(f"ERROR: Firebase initialization failed: {e}")
            self.db = None
    
    def _use_env_credentials(self):
        """Try to initialize Firebase using environment variables."""
        try:
            # Check if required environment variables exist
            project_id = os.getenv('FIREBASE_PROJECT_ID') or 'mybro-a1ea5'
            private_key = os.getenv('FIREBASE_PRIVATE_KEY')
            client_email = os.getenv('FIREBASE_CLIENT_EMAIL')
            
            if not all([private_key, client_email]):
                return False
            
            # Create credentials from environment variables
            cred_dict = {
                "type": "service_account",
                "project_id": project_id,
                "private_key": private_key.replace('\\n', '\n'),
                "client_email": client_email,
                "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                "token_uri": "https://oauth2.googleapis.com/token",
            }
            
            cred = credentials.Certificate(cred_dict)
            firebase_admin.initialize_app(cred)
            return True
            
        except Exception as e:
            print(f"Environment credentials failed: {e}")
            return False
    
    def _use_service_account_file(self):
        """Try to initialize Firebase using service account file."""
        try:
            cred = credentials.Certificate("mybro-a1ea5-firebase-adminsdk-5a3xf-6089092d21.json")
            firebase_admin.initialize_app(cred)
            return True
        except Exception as e:
            print(f"Service account file failed: {e}")
            return False
    
    # ==================== USER PROFILE OPERATIONS ====================
    
    def create_user_profile(self, email: str, name: str = None) -> UserProfile:
        """Create a new user profile using email as document ID in users collection."""
        profile_data = {
            "name": name
        }
        
        if self.db:
            try:
                self.db.collection('users').document(email).set(profile_data)
                print(f"SUCCESS: Created user profile for {email}")
            except Exception as e:
                print(f"ERROR: Error creating user profile: {e}")
        
        return UserProfile(
            name=name
        )
    
    def get_user_profile(self, email: str) -> UserProfile:
        """Get user profile from Firestore using email as document ID."""
        if self.db:
            try:
                # Get profile data from users collection
                doc_ref = self.db.collection('users').document(email)
                doc = doc_ref.get()
                
                if doc.exists:
                    user_data = doc.to_dict()
                    return UserProfile(
                        name=user_data.get('name')
                    )
            except Exception as e:
                print(f"ERROR: Error getting user profile: {e}")
        
        return UserProfile(
            name="Unknown"
        )
    
    def update_user_profile(self, email: str, updates: Dict[str, Any]):
        """Update user profile in Firestore using email as document ID."""
        # Define allowed profile fields only
        allowed_fields = {
            'name'
        }
        
        if self.db:
            try:
                # Only allow authorized fields, reject everything else
                profile_updates = {
                    k: v for k, v in updates.items() 
                    if k in allowed_fields
                }
                
                if profile_updates:
                    self.db.collection('users').document(email).update(profile_updates)
                    print(f"SUCCESS: Updated user profile for {email} with fields: {list(profile_updates.keys())}")
                else:
                    print(f"INFO: No authorized fields to update for {email}")
                    
            except Exception as e:
                print(f"ERROR: Error updating user profile: {e}")
    
    # ==================== CONVERSATION OPERATIONS ====================
    
    def add_chat_pair(self, email: str, user_message: str, model_response: str, 
                      emotion_detected: str = None, urgency_level: int = 1):
        """Add a chat pair (user + model response) to Firestore."""
        if not self.db:
            return
        
        try:
            now = datetime.now()
            conversation_id = f"conv_{now.strftime('%Y%m%d')}"
            
            chat_pair_data = {
                "user": user_message,
                "model": model_response,
                "timestamp": firestore.SERVER_TIMESTAMP,
                "emotion_detected": emotion_detected,  # Use snake_case consistently
                "urgency_level": urgency_level         # Use snake_case consistently
            }
            
            # Add chat pair to user's conversation subcollection
            self.db.collection('users').document(email).collection('conversations').document(conversation_id).collection('chat').add(chat_pair_data)
            
            # Update conversation metadata
            conv_doc_ref = self.db.collection('users').document(email).collection('conversations').document(conversation_id)
            conv_doc = conv_doc_ref.get()
            
            if conv_doc.exists:
                existing_metadata = conv_doc.to_dict()
                pair_count = existing_metadata.get('chatPairCount', 0) + 1
                message_count = existing_metadata.get('messageCount', 0) + 2  # Each pair adds 2 messages
            else:
                pair_count = 1
                message_count = 2  # First pair = 2 messages
            
            metadata = {
                "startDate": now.strftime('%Y-%m-%d'),
                "chatPairCount": pair_count,
                "messageCount": message_count,
                "lastChatAt": firestore.SERVER_TIMESTAMP,
                "lastMessageAt": firestore.SERVER_TIMESTAMP
            }
            
            conv_doc_ref.set(metadata, merge=True)
            print(f"SUCCESS: Added chat pair to {email}'s conversation")
            
        except Exception as e:
            print(f"ERROR: Error adding chat pair: {e}")
    
    def add_message(self, email: str, role: str, content: str, 
                   emotion_detected: str = None, urgency_level: int = 1):
        """Legacy method - deprecated. Use add_chat_pair instead."""
        print("WARNING: add_message is deprecated. Use add_chat_pair instead.")
        # Keep for backward compatibility but don't actually store anything
        pass
    
    def get_recent_chat(self, email: str, limit: int = 10) -> List[ChatPair]:
        """Get recent chat pairs from Firestore."""
        if not self.db:
            return []
        
        try:
            today = datetime.now().strftime('%Y%m%d')
            conversation_id = f"conv_{today}"
            
            chat_ref = self.db.collection('users').document(email).collection('conversations').document(conversation_id).collection('chat')
            chat = chat_ref.order_by('timestamp', direction=firestore.Query.DESCENDING).limit(limit).stream()
            
            chat_pair_list = []
            for doc in chat:
                pair_data = doc.to_dict()
                # Create ChatPair object from the data (handle both formats for compatibility)
                chat_pair = ChatPair(
                    user=pair_data.get('user', ''),
                    model=pair_data.get('model', ''),
                    timestamp=pair_data.get('timestamp', datetime.now()),
                    emotion_detected=pair_data.get('emotion_detected') or pair_data.get('emotionDetected'),
                    urgency_level=pair_data.get('urgency_level') or pair_data.get('urgencyLevel', 1)
                )
                chat_pair_list.append(chat_pair)
            
            # Reverse to get chronological order (oldest first)
            chat_pair_list.reverse()
            return chat_pair_list
            
        except Exception as e:
            print(f"ERROR: Error getting recent chat pairs: {e}")
        
        return []

    def get_recent_messages(self, email: str, limit: int = 10) -> List[Dict]:
        """Legacy method - deprecated. Use get_recent_chat instead."""
        print("WARNING: get_recent_messages is deprecated. Use get_recent_chat instead.")
        return []
    
    # ==================== EVENT OPERATIONS ====================
    
    def add_important_event(self, email: str, event: ImportantEvent):
        """Add an important event to Firestore using subcollection."""
        if not self.db:
            return
        
        try:
            event_data = {
                "eventType": event.eventType,
                "description": event.description,
                "eventDate": event.eventDate,
                "mentionedAt": event.mentionedAt,
                "followUpNeeded": event.followUpNeeded,
                "followUpDone": event.followUpDone
            }
            
            # Generate a unique document ID since we no longer have event_id in the model
            doc_ref = self.db.collection('users').document(email).collection('events').document()
            doc_ref.set(event_data)
            print(f"SUCCESS: Added event for {email}: {event.eventType}")
            
        except Exception as e:
            print(f"ERROR: Error adding event: {e}")
    
    def get_pending_events(self, email: str) -> List[Dict]:
        """Get events that need follow-up for user."""
        if not self.db:
            return []
        
        try:
            events_ref = self.db.collection('users').document(email).collection('events')
            events = events_ref.where(filter=FieldFilter('followUpNeeded', '==', True)).where(filter=FieldFilter('followUpDone', '==', False)).stream()
            
            pending_events = []
            for doc in events:
                event_data = doc.to_dict()
                event_data['event_id'] = doc.id
                pending_events.append(event_data)
            
            return pending_events
            
        except Exception as e:
            print(f"ERROR: Error getting pending events: {e}")
        
        return []
    
    def mark_event_followed_up(self, email: str, event_type: str) -> None:
        """Mark events as followed up after asking about them."""
        if not self.db:
            return
        
        try:
            events_ref = self.db.collection('users').document(email).collection('events')
            events = events_ref.where(filter=FieldFilter('eventType', '==', event_type)).where(filter=FieldFilter('followUpDone', '==', False)).stream()
            
            for doc in events:
                # Mark this event as followed up
                doc.reference.update({'followUpDone': True})
                print(f"SUCCESS: Marked event as followed up: {event_type}")
                break
            
        except Exception as e:
            print(f"ERROR: Error marking event as followed up: {e}")
    
    # ==================== USER DISCOVERY OPERATIONS ====================
    
    def get_all_user_profiles(self) -> Dict[str, UserProfile]:
        """Get all user profiles from Firestore."""
        try:
            users_ref = self.db.collection('users')
            docs = users_ref.stream()
            profiles = {}
            
            for doc in docs:
                user_data = doc.to_dict()
                email = doc.id  # Document ID is the email
                if user_data:
                    profile = UserProfile(
                        name=user_data.get('name')
                    )
                    profiles[email] = profile
            
            return profiles
            
        except Exception as e:
            print(f"ERROR: Error getting all user profiles: {e}")
            return {}

    # ==================== DAILY SUMMARY OPERATIONS ====================
    
    def get_last_conversation_date(self, email: str) -> Optional[date]:
        """Get the date of user's last conversation."""
        if not self.db:
            return None
        
        try:
            conversations_ref = self.db.collection('users').document(email).collection('conversations')
            conversations = conversations_ref.stream()
            
            conversation_dates = []
            for doc in conversations:
                conv_id = doc.id
                if conv_id.startswith('conv_'):
                    date_str = conv_id.replace('conv_', '')
                    try:
                        conv_date = datetime.strptime(date_str, '%Y%m%d').date()
                        conversation_dates.append(conv_date)
                    except ValueError:
                        continue
            
            if conversation_dates:
                return max(conversation_dates)
            
            return None
            
        except Exception as e:
            print(f"ERROR: Error getting last conversation date: {e}")
            return None
    
    def daily_summary_exists(self, email: str, date_str: str) -> bool:
        """Check if a daily summary already exists for the given date."""
        if not self.db:
            return False
        
        try:
            doc_ref = self.db.collection('users').document(email).collection('summaries').document(f'daily_{date_str}')
            doc = doc_ref.get()
            return doc.exists
            
        except Exception as e:
            print(f"ERROR: Error checking daily summary existence: {e}")
            return False
    
    def get_conversation_by_date(self, email: str, date_str: str) -> Optional[dict]:
        """Get conversation data for a specific date, including chat pairs as messages."""
        if not self.db:
            return None
        
        try:
            conversation_id = f"conv_{date_str}"
            doc_ref = self.db.collection('users').document(email).collection('conversations').document(conversation_id)
            doc = doc_ref.get()
            
            if doc.exists:
                conversation = doc.to_dict()
                conversation['date'] = date_str
                
                # Get chat pairs and convert them to messages format for summarization
                chat_ref = doc_ref.collection('chat')
                pairs = list(chat_ref.order_by('timestamp').stream())
                
                messages = {}
                message_counter = 1
                
                for pair in pairs:
                    pair_data = pair.to_dict()
                    
                    # Add user message
                    user_msg_id = f"msg_{message_counter}"
                    messages[user_msg_id] = {
                        'role': 'user',
                        'content': pair_data.get('user', ''),
                        'timestamp': pair_data.get('timestamp'),
                        'emotionDetected': pair_data.get('emotion_detected') or pair_data.get('emotionDetected'),
                        'urgencyLevel': pair_data.get('urgency_level') or pair_data.get('urgencyLevel', 1)
                    }
                    message_counter += 1
                    
                    # Add assistant message
                    assistant_msg_id = f"msg_{message_counter}"
                    messages[assistant_msg_id] = {
                        'role': 'assistant',
                        'content': pair_data.get('model', ''),
                        'timestamp': pair_data.get('timestamp')
                    }
                    message_counter += 1
                
                conversation['messages'] = messages
                return conversation
            
            return None
            
        except Exception as e:
            print(f"ERROR: Error getting conversation by date: {e}")
            return None
    
    def store_daily_summary(self, email: str, date_str: str, summary: dict):
        """Store a daily conversation summary."""
        if not self.db:
            return
        
        try:
            self.db.collection('users').document(email).collection('summaries').document(f'daily_{date_str}').set(summary)
            print(f"SUCCESS: Stored daily summary for {email} on {date_str}")
            
        except Exception as e:
            print(f"ERROR: Error storing daily summary: {e}")
    
    def get_daily_summary(self, email: str, date_str: str) -> Optional[dict]:
        """Get daily summary for a specific date."""
        if not self.db:
            return None
        
        try:
            doc_ref = self.db.collection('users').document(email).collection('summaries').document(f'daily_{date_str}')
            doc = doc_ref.get()
            
            if doc.exists:
                return doc.to_dict()
            return None
            
        except Exception as e:
            print(f"ERROR: Error getting daily summary: {e}")
            return None
    
    def get_all_summaries(self, email: str) -> List[Dict]:
        """Get all conversation summaries for a user, ordered by date."""
        if not self.db:
            return []
        
        try:
            summaries_ref = self.db.collection('users').document(email).collection('summaries')
            summaries = summaries_ref.order_by('date', direction=firestore.Query.DESCENDING).stream()
            
            summary_list = []
            for doc in summaries:
                summary_data = doc.to_dict()
                summary_data['document_id'] = doc.id
                summary_list.append(summary_data)
            
            return summary_list
            
        except Exception as e:
            print(f"ERROR: Error getting all summaries: {e}")
            return []

    def find_user_by_name(self, name: str) -> Optional[UserProfile]:
        """Find user profile by name (case insensitive)."""
        try:
            all_profiles = self.get_all_user_profiles()
            name_lower = name.lower()
            
            for profile in all_profiles.values():
                if profile.name and profile.name.lower() == name_lower:
                    return profile
            
            return None
            
        except Exception as e:
            print(f"ERROR: Error finding user by name: {e}")
            return None

# Global Firebase manager instance
firebase_manager = FirebaseManager()