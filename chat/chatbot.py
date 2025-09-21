import random
import asyncio
from datetime import datetime, date
from typing import List, Optional
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import HumanMessage, SystemMessage, AIMessage
from models import ChatResponse, UserProfile, ChatPair, ImportantEvent
from memory import MemoryManager
from filter import MentalHealthFilter
from config import config



class MentalHealthChatbot:
    """Main chatbot class that orchestrates the mental health conversation."""
    
    def __init__(self):
        self.llm = ChatGoogleGenerativeAI(
            model=config.model_name,
            google_api_key=config.gemini_api_key,
            temperature=config.temperature,
            max_tokens=config.max_tokens
        )
        
        self.memory_manager = MemoryManager()
        self.health_filter = MentalHealthFilter()
        
        self.system_prompt = self._create_system_prompt()
    
    def _create_system_prompt(self) -> str:
        """Create the system prompt that defines the chatbot's personality."""
        return """You are MyBro - a caring, supportive friend who adapts your response style based on what the person needs. Your personality adjusts to match the situation:

        ⏰ TIME AWARENESS - VERY IMPORTANT:
        - ALWAYS acknowledge when time has passed since your last conversation
        - If they haven't talked in 1+ days, mention it: "Haven't heard from you since yesterday, how are you holding up?"
        - If it's been several days: "Man, it's been 3 days! I was worried about you. How have you been?"
        - Reference time naturally: "Last time we talked..." "Since yesterday..." "A few days ago you mentioned..."
        - If it's the same day: "Earlier today you said..." "A few hours ago..."
        - Use the time context provided to show you care and remember their timeline

        🎭 ADAPTIVE RESPONSE LEVELS:

        🟢 CASUAL/POSITIVE CONVERSATIONS (when they're sharing good news, casual chat, mild stress):
        - Be a supportive, chill friend 
        - Use encouraging language but don't overreact
        - Ask follow-up questions naturally
        - Match their energy level - if they're casual, be casual
        - Example: "That's awesome, man! How did that make you feel?" "Sounds like you're handling things well"

        🟡 MILD CONCERN (everyday stress, minor worries, feeling down but not severe):
        - Be more attentive and caring
        - Offer gentle support and encouragement  
        - Ask deeper questions but don't assume crisis
        - Provide perspective and coping suggestions
        - Example: "That sounds tough, bro. Want to talk about what's making you feel this way?"

        🟠 MODERATE DISTRESS (significant anxiety, depression symptoms, relationship issues):
        - Show more emotional investment
        - Be more direct about caring and support
        - Challenge negative thoughts gently but firmly
        - Remind them of their strengths and support system
        - Example: "Hey, I can tell this is really affecting you. You don't have to go through this alone"

        � CRISIS MODE (suicidal thoughts, severe depression, immediate danger):
        - NOW you become passionate and protective
        - Fight back against harmful thoughts aggressively but lovingly
        - Remind them of people who love them (family, friends, partners)
        - Challenge their negative thoughts directly: "That's not true, bro, and you know it"
        - Be their protector: "I'm not letting you think like that, man"
        - Show urgency about their wellbeing

        🛡️ CRISIS INTERVENTION EXAMPLES:
        "Bro, STOP. Your mom said that because she's scared and stressed, not because she doesn't love you!"
        "Listen to me - you are NOT going anywhere! Your family needs you, even if they're bad at showing it right now."
        "No way, man! You think your dad sacrificed everything just to lose his son? Hell no!"

        💡 KEY PRINCIPLE: MATCH THE ENERGY AND NEED
        - Don't treat someone sharing good news like they're in crisis
        - Don't treat casual frustration like severe depression  
        - Escalate your intensity only when the situation truly calls for it
        - Be supportive without being overwhelming

        🤗 CARING CONTEXTUAL QUESTIONS (Ask these AFTER building rapport, not immediately):
        When someone seems stressed/sad/troubled, gradually ask about:
        - Basic care: "Have you been eating okay?" "How's your sleep been lately?"
        - Relationships: "Everything okay with family?" "How are things with your girlfriend/boyfriend?"
        - Life context: "What's been going on at school/work?" "Did something happen with your parents?"
        - Support system: "Do you have friends you can talk to about this?"

        ⏰ TIMING FOR DEEPER QUESTIONS:
        - NEVER ask personal questions in the first 1-2 exchanges
        - Wait until they've shared something emotional or concerning
        - Build on what they tell you naturally
        - If they mention being sad, THEN ask what happened
        - If they seem stressed, THEN explore the source

        EXAMPLE PROGRESSION:
        User: "I'm feeling really down"
        You: "I'm sorry to hear that, bro. What's been going on?"
        User: [shares more]
        You: "That sounds tough. How have you been sleeping through all this?" OR "Have you talked to anyone close to you about this?"

        Remember: You can be caring and supportive without being aggressive. Save the intense, protective energy for when someone actually needs saving."""

    async def chat(self, email: str, message: str) -> ChatResponse:
        """Main chat method that processes user input and generates response."""
        
        # Check if we should generate a proactive greeting first
        proactive_greeting = self._generate_proactive_greeting(email)
        
        # Check if message is mental health related
        topic_filter = self.health_filter.is_mental_health_related(message)
        
        if not topic_filter.is_mental_health_related:
            redirect_response = "Sorry but i can not answer to that question!!!."
            
            # Still save the interaction but with a redirect
            self.memory_manager.add_message(email, "user", message)
            self.memory_manager.add_message(email, "assistant", redirect_response)
            
            return ChatResponse(
                message=redirect_response,
                emotion_tone="neutral",
                suggestions=["Tell me how you're feeling today", "Share what's on your mind"],
                follow_up_questions=["How are you doing emotionally?", "What's been on your mind lately?"]
            )
        
        # Detect emotion and urgency
        emotion, urgency_level = self.health_filter.detect_emotion(message)
        
        # Detect and store important events
        self._detect_important_events(message, email)
        
        # Get conversation context BEFORE adding the current message
        context = self.memory_manager.get_conversation_context(email)
        user_profile = self.memory_manager.get_user_profile(email)
        
        # Check if this is a crisis situation - only trigger for urgency level 5 (extreme)
        if urgency_level >= 5:
            crisis_response = self._handle_crisis_situation(message, user_profile.name)
            
            # Add user message to memory
            self.memory_manager.add_message(
                email, "user", message, 
                emotion_detected=emotion, 
                urgency_level=urgency_level
            )
            
            # Add crisis response to memory
            self.memory_manager.add_message(email, "assistant", crisis_response.message)
            return crisis_response
        
        # Build conversation for LLM
        conversation_history = self._build_conversation_history(email)
        recent_messages = self.memory_manager.get_recent_messages(email, 20)
        conversation_depth = len(recent_messages)
        
        # Create the prompt with context
        enhanced_prompt = f"""{self.system_prompt}

        CONVERSATION CONTEXT:
        {context}

        {f"PROACTIVE GREETING: You should start your response with this caring follow-up: '{proactive_greeting}'" if proactive_greeting else ""}

        CURRENT USER STATE:
        - Detected emotion: {emotion}
        - Urgency level: {urgency_level}/5
        - User prefers to be called: {user_profile.name}
        - Conversation depth: {conversation_depth} messages (deeper conversations allow more personal questions)

        🎯 RESPONSE GUIDANCE BASED ON URGENCY LEVEL:

        Level 1-2 (Casual/Mild): Be supportive but relaxed. Don't overreact. Match their energy level.
        Level 3 (Moderate): Show more concern and support. Ask deeper questions but stay calm.
        Level 4-5 (Crisis): NOW use your passionate, protective mode. Fight for them!

        🤗 CONVERSATION DEPTH GUIDANCE:
        - First 1-2 exchanges: Keep it general, build rapport
        - 3-5 exchanges: Start exploring their situation more
        - 6+ exchanges with emotional content: NOW you can ask about sleep, food, family, relationships naturally

        IMPORTANT: Do not assume crisis or depression unless urgency level is 3+. For levels 1-2, be a normal supportive friend.

        Remember to:
        1. Address them by their preferred name
        2. Reference relevant past conversations
        3. Match your tone to their ACTUAL emotional state (don't assume worst case)
        4. Only escalate intensity if urgency level is high
        5. Acknowledge time passed since last conversation if applicable
        6. If there's a proactive greeting above, start with that and then naturally flow into responding to their current message"""
        
        # Build message list for the LLM
        messages = [
            SystemMessage(content=enhanced_prompt),
            *conversation_history,
            HumanMessage(content=message)
        ]
        
        try:
            # Get response from LLM
            response = await self.llm.ainvoke(messages)
            bot_message = response.content
            
            # Generate follow-up questions and suggestions
            follow_up_questions = self._generate_follow_up_questions(emotion, urgency_level, user_profile.name, email, message)
            suggestions = self._generate_suggestions(emotion, urgency_level, message, user_profile.name)
            
            # If we used a proactive greeting, mark relevant events as followed up
            if proactive_greeting:
                # Extract event type from greeting to mark as followed up
                for event_type in ['exam', 'interview', 'appointment']:
                    if event_type in proactive_greeting.lower():
                        self._mark_event_followed_up(email, event_type)
                        break
            
            # Add user message to memory first
            self.memory_manager.add_message(
                email, "user", message, 
                emotion_detected=emotion, 
                urgency_level=urgency_level
            )
            
            # Add assistant response to memory
            self.memory_manager.add_message(email, "assistant", bot_message)
            
            return ChatResponse(
                message=bot_message,
                emotion_tone=emotion,
                suggestions=suggestions,
                follow_up_questions=follow_up_questions
            )
            
        except Exception as e:
            # Generate contextual error message using LLM
            error_message = self._generate_error_response_with_llm(message, emotion, urgency_level, user_profile.name)
            error_suggestions = self._generate_error_suggestions_with_llm(message, emotion)
            error_questions = self._generate_error_follow_up_questions_with_llm(user_profile.name)
            
            # Add user message to memory
            self.memory_manager.add_message(
                email, "user", message, 
                emotion_detected=emotion, 
                urgency_level=urgency_level
            )
            
            # Add error response to memory
            self.memory_manager.add_message(email, "assistant", error_message)
            
            return ChatResponse(
                message=error_message,
                emotion_tone="supportive",
                suggestions=error_suggestions,
                follow_up_questions=error_questions
            )

    def _handle_crisis_situation(self, message: str, user_name: str) -> ChatResponse:
        """Handle crisis situations with immediate support and resources using LLM."""
        name = user_name or "friend"
        
        # Generate crisis response using LLM
        crisis_message = self._generate_crisis_response_with_llm(message, name)
        
        # Generate crisis-specific suggestions and follow-up questions
        suggestions = self._generate_crisis_suggestions_with_llm(message, name)
        follow_up_questions = self._generate_crisis_follow_up_questions_with_llm(message, name)
        
        return ChatResponse(
            message=crisis_message,
            emotion_tone="urgent",
            suggestions=suggestions,
            follow_up_questions=follow_up_questions
        )

    def _generate_crisis_response_with_llm(self, message: str, name: str) -> str:
        """Generate personalized crisis intervention response using LLM."""
        system_prompt = f"""You are MyBro, a caring friend responding to someone in severe emotional crisis. Generate a compassionate, urgent, but caring crisis intervention response that:

        1. IMMEDIATELY shows deep concern and love for them
        2. Acknowledges their pain without minimizing it
        3. Fights against harmful thoughts with protective, loving energy
        4. Includes essential crisis resources (MUST include these exactly):
           - Call 988 (Suicide & Crisis Lifeline) - Available 24/7
           - Text HOME to 741741 (Crisis Text Line)
           - Call 911 if in immediate danger
           - Go to nearest emergency room
        5. Emphasizes their value and that people care about them
        6. Shows urgency about getting help TODAY
        7. Uses their name naturally and personally

        TONE GUIDELINES:
        - Be passionately protective, like fighting for a family member
        - Show genuine fear for their safety while remaining strong
        - Be direct and urgent but not clinical
        - Challenge negative thoughts with love and reality
        - Make it personal - this is about THEM specifically

        STRUCTURE:
        - Start with immediate, caring concern that uses their name
        - Acknowledge their crisis and pain
        - List the crisis resources clearly (use the exact format above)
        - End with personal, urgent plea for them to reach out TODAY

        USER CONTEXT:
        - Name: {name}
        - Crisis message: "{message}"
        
        Generate a powerful, loving crisis intervention response that could save their life."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Generate a crisis intervention response for {name} who said: '{message}'")
            ]
            
            response = self.llm.invoke(messages)
            return response.content.strip()
            
        except Exception as e:
            # Critical fallback for crisis situations
            return f"""{name}, I'm genuinely scared for you right now, but I also know you're stronger than this moment. You reached out to me, which means part of you is still fighting.

Listen to me: You're in crisis right now, and that's okay - it happens to the strongest people. But you don't have to face this alone.

**Please reach out to someone who can help immediately:**
• **Call 988** (Suicide & Crisis Lifeline) - Available 24/7
• **Text HOME to 741741** (Crisis Text Line)
• **Call 911** if you're in immediate danger
• **Go to your nearest emergency room**

{name}, I need you to promise me you'll reach out to one of these resources today. Your life has value, and people care about you more than you know right now."""

    def _generate_crisis_suggestions_with_llm(self, message: str, name: str) -> List[str]:
        """Generate crisis-specific suggestions using LLM."""
        system_prompt = f"""Generate 4 immediate, actionable crisis intervention suggestions for someone in severe emotional distress. These should be:

        1. IMMEDIATE safety-focused actions they can take right now
        2. Specific and actionable (not vague)
        3. Appropriate for crisis level urgency
        4. Mix of professional help and personal support
        5. Focus on TODAY - immediate actions

        USER CONTEXT:
        - Name: {name}
        - Crisis situation: "{message}"

        Return exactly 4 suggestions, one per line, without numbering."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Generate crisis suggestions for {name}: '{message}'")
            ]
            
            response = self.llm.invoke(messages)
            suggestions_text = response.content.strip()
            
            suggestions = [s.strip() for s in suggestions_text.split('\n') if s.strip()]
            return suggestions[:4] if len(suggestions) >= 4 else [
                "Call 988 Suicide & Crisis Lifeline",
                "Text HOME to 741741",
                "Call a trusted friend or family member",
                "Go to the nearest emergency room"
            ]
            
        except Exception as e:
            return [
                "Call 988 Suicide & Crisis Lifeline",
                "Text HOME to 741741", 
                "Call a trusted friend or family member",
                "Go to the nearest emergency room"
            ]

    def _generate_crisis_follow_up_questions_with_llm(self, message: str, name: str) -> List[str]:
        """Generate crisis-specific follow-up questions using LLM."""
        system_prompt = f"""Generate 2 caring but urgent follow-up questions for someone in crisis. These should:

        1. Check their immediate safety and support systems
        2. Encourage immediate action for getting help
        3. Be personal and caring, using their name
        4. Focus on RIGHT NOW - immediate needs
        5. Help assess their current safety situation

        USER CONTEXT:
        - Name: {name}
        - Crisis message: "{message}"

        Return exactly 2 questions, one per line."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Generate crisis follow-up questions for {name}: '{message}'")
            ]
            
            response = self.llm.invoke(messages)
            questions_text = response.content.strip()
            
            questions = [q.strip() for q in questions_text.split('\n') if q.strip() and '?' in q]
            return questions[:2] if len(questions) >= 2 else [
                "Can you call someone to be with you right now?",
                "Do you have the 988 number saved in your phone?"
            ]
            
        except Exception as e:
            return [
                "Can you call someone to be with you right now?",
                "Do you have the 988 number saved in your phone?"
            ]

    def _build_conversation_history(self, email: str) -> List:
        """Build conversation history for the LLM."""
        recent_messages = self.memory_manager.get_recent_messages(email, 10)
        
        langchain_messages = []
        for msg in recent_messages:
            if msg.role == "user":
                langchain_messages.append(HumanMessage(content=msg.content))
            elif msg.role == "assistant":
                langchain_messages.append(AIMessage(content=msg.content))
        
        return langchain_messages

    def _detect_important_events(self, message: str, email: str) -> None:
        """Detect and store important upcoming events from user messages using LLM."""
        
        # Use LLM to detect events and timing
        event_detection = self._extract_events_with_llm(message)
        
        if event_detection and event_detection.get('has_event'):
            from datetime import date, timedelta
            import uuid
            
            # Calculate event date based on LLM timing analysis
            event_date = self._parse_event_timing(event_detection.get('timing', ''), message)
            
            # Create and store the event
            event = ImportantEvent(
                eventType=event_detection.get('event_type', 'event'),
                description=message,
                eventDate=event_date.isoformat() if event_date else None,
                followUpNeeded=True,
                followUpDone=False
            )
            
            # Store event directly in Firebase events table
            from firebase_manager import firebase_manager
            firebase_manager.add_important_event(email, event)
            # Event detected and stored successfully
            
    def _extract_events_with_llm(self, message: str) -> Optional[dict]:
        """Use LLM to extract important events and timing from user messages."""
        system_prompt = """You are an expert at detecting important upcoming events or recent events that someone might want follow-up on. Analyze the user's message and determine:

        1. If there's an important event mentioned (exam, interview, appointment, date, presentation, meeting, deadline, party, etc.)
        2. The type of event (be specific but use common categories)
        3. The timing context (when it's happening or happened)

        IMPORTANT: Only detect events that are:
        - Significant enough that a caring friend would follow up about
        - Have clear timing indicators (today, tomorrow, next week, yesterday, etc.)
        - Are specific events, not general activities

        Return your analysis in this EXACT JSON format:
        {
            "has_event": true/false,
            "event_type": "exam" or "interview" or "appointment" or "date" or "presentation" or "meeting" or "deadline" or "party" or "other",
            "timing": "today" or "tomorrow" or "yesterday" or "next week" or "this weekend" or "next month" or "specific timing phrase",
            "confidence": 0.0-1.0
        }

        Only return has_event: true if you're confident (>0.7) there's a real important event with timing."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Analyze this message for important events: '{message}'")
            ]
            
            response = self.llm.invoke(messages)
            response_text = response.content.strip()
            
            # Parse JSON response
            import json
            try:
                # Extract JSON from response if it's wrapped in text
                if '{' in response_text and '}' in response_text:
                    start = response_text.find('{')
                    end = response_text.rfind('}') + 1
                    json_str = response_text[start:end]
                    event_data = json.loads(json_str)
                    
                    # Validate the response structure
                    if isinstance(event_data, dict) and 'has_event' in event_data:
                        confidence = event_data.get('confidence', 0.0)
                        if event_data.get('has_event') and confidence >= 0.7:
                            return event_data
                        
            except json.JSONDecodeError:
                pass
                
            return None
            
        except Exception as e:
            return None

    def _parse_event_timing(self, timing: str, original_message: str) -> Optional[date]:
        """Parse timing information to determine event date."""
        from datetime import date, timedelta
        
        today = date.today()
        timing_lower = timing.lower()
        message_lower = original_message.lower()
        
        # LLM-provided timing
        if 'tomorrow' in timing_lower:
            return today + timedelta(days=1)
        elif 'today' in timing_lower or 'tonight' in timing_lower:
            return today
        elif 'yesterday' in timing_lower:
            return today - timedelta(days=1)
        elif 'next week' in timing_lower:
            return today + timedelta(days=7)
        elif 'this weekend' in timing_lower:
            return today + timedelta(days=(5 - today.weekday()) if today.weekday() < 5 else 1)
        elif 'next month' in timing_lower:
            return today + timedelta(days=30)
        
        # Fallback to original message analysis for specific days
        days_of_week = {
            'monday': 0, 'tuesday': 1, 'wednesday': 2, 'thursday': 3,
            'friday': 4, 'saturday': 5, 'sunday': 6
        }
        
        for day_name, day_num in days_of_week.items():
            if f'next {day_name}' in message_lower:
                days_ahead = day_num - today.weekday()
                if days_ahead <= 0:
                    days_ahead += 7
                return today + timedelta(days=days_ahead)
        
        return None

    def _generate_proactive_greeting(self, email: str) -> Optional[str]:
        """Generate a personalized proactive greeting using LLM for important events."""
        user_profile = self.memory_manager.get_user_profile(email)
        name = user_profile.name or "friend"
        
        from datetime import date, timedelta
        from firebase_manager import firebase_manager
        today = date.today()
        yesterday = today - timedelta(days=1)
        
        # STEP 1: Check if we need to generate yesterday's summary (first chat of new day)
        self._generate_daily_summary_if_needed(email)
        
        # STEP 2: Get pending events from Firebase events table
        pending_events = firebase_manager.get_pending_events(email)
        
        for event_data in pending_events:
            # Convert event_data to proper format for checking
            #event_date = None
            event_date_str = event_data.get('eventDate')
            if isinstance(event_date_str, str) and event_date_str:
                        from datetime import datetime
                        event_date = datetime.fromisoformat(event_date_str).date()
            else:
                        event_date = None
            
            # Determine the timing context
            timing_context = ""
            if event_date == today:
                timing_context = "today"
            elif event_date == yesterday:
                timing_context = "yesterday"
            elif event_date and event_date > today and (event_date - today).days <= 2:
                days_until = (event_date - today).days
                timing_context = f"in {days_until} day{'s' if days_until > 1 else ''}"
            else:
                continue  # Skip events outside our follow-up window
            
            # Create temporary event object for greeting generation
            event = ImportantEvent(
                eventType=event_data['eventType'],
                description=event_data['description'],
                eventDate=event_date.isoformat() if event_date else None,
                mentionedAt=event_data.get('mentionedAt', ''),
                followUpNeeded=event_data.get('followUpNeeded', True),
                followUpDone=event_data.get('followUpDone', False)
            )
            
            # Generate personalized greeting using LLM
            return self._generate_event_greeting_with_llm(event, name, timing_context)
        
        return None

    def _generate_event_greeting_with_llm(self, event, name: str, timing_context: str) -> str:
        """Generate a personalized event greeting using LLM."""
        system_prompt = f"""You are MyBro, a caring friend who remembers important events in people's lives. Generate a warm, personalized greeting that asks about an important event. 

        GUIDELINES:
        - Be genuinely caring and show you remember the event
        - Use natural, friendly language like you're texting a close friend
        - Show appropriate emotion (excitement, concern, encouragement) for the event type
        - Keep it conversational and warm, not formal
        - Reference the timing naturally
        - Make it feel personal and thoughtful

        EVENT CONTEXT:
        - Person's name: {name}
        - Event type: {event.eventType}
        - Timing: {timing_context}
        - Event description: {event.description if hasattr(event, 'description') else 'Not available'}

        TIMING MEANINGS:
        - "today": Event happened today, ask how it went
        - "yesterday": Event happened yesterday, follow up on how it went
        - "in X days": Event is upcoming, check how they're feeling about it

        Generate ONE natural, caring greeting message that shows you remember and care about their event."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Generate a caring greeting for {name} about their {event.eventType} that happened/happens {timing_context}")
            ]
            
            response = self.llm.invoke(messages)
            greeting = response.content.strip()
            
            # Remove any quotes that might wrap the response
            if greeting.startswith('"') and greeting.endswith('"'):
                greeting = greeting[1:-1]
            
            return greeting
            
        except Exception as e:
            # Simple fallback if LLM fails
            if timing_context in ["today", "yesterday"]:
                return f"Hey {name}! How did your {event.eventType} go {timing_context}?"
            else:
                return f"Hey {name}! How are you feeling about your upcoming {event.eventType}?"

    def _mark_event_followed_up(self, email: str, event_type: str) -> None:
        """Mark events as followed up after asking about them."""
        from firebase_manager import firebase_manager
        firebase_manager.mark_event_followed_up(email, event_type)

    def _generate_follow_up_questions(self, emotion: str, urgency_level: int, user_name: str, email: str, user_message: str = "") -> List[str]:
        """Generate personalized follow-up questions using LLM based on emotion, urgency, and conversation context."""
        name = user_name or "friend"
        
        # Get conversation context
        recent_messages = self.memory_manager.get_recent_messages(email, 10)
        conversation_depth = len(recent_messages)
        
        # Build conversation history for context
        conversation_context = ""
        if recent_messages:
            for msg in recent_messages[-5:]:  # Last 5 messages for context
                role = "User" if msg.role == "user" else "Assistant"
                conversation_context += f"{role}: {msg.content}\n"
        
        system_prompt = f"""You are a caring mental health companion generating thoughtful follow-up questions. Based on the user's current emotional state, conversation history, and relationship depth, create 2-3 empathetic follow-up questions that:

        1. Show genuine care and interest in their situation
        2. Are appropriate for their emotional state and urgency level
        3. Consider the conversation depth and intimacy level
        4. Help them explore their feelings or situation deeper
        5. Feel natural and conversational, not clinical

        CONTEXT:
        - User's name: {name}
        - Current emotion: {emotion}
        - Urgency level: {urgency_level}/5 (1=casual, 2=mild concern, 3=moderate distress, 4=high distress, 5=crisis)
        - Conversation depth: {conversation_depth} messages exchanged

        CONVERSATION GUIDELINES BY URGENCY:
        - Level 1-2: Casual, supportive questions that show interest
        - Level 3: More caring questions about their well-being and situation
        - Level 4-5: Questions focused on safety, support systems, and immediate needs

        CONVERSATION DEPTH GUIDELINES:
        - Early conversation (1-3 messages): General, rapport-building questions
        - Developing relationship (4-10 messages): More personal but still gentle questions  
        - Deeper relationship (10+ messages): Can ask about family, relationships, self-care naturally

        QUESTION STYLE:
        - Use {name} naturally in questions when appropriate
        - Be conversational, not interrogating
        - Mix emotional support with practical exploration
        - Show you remember and care about their situation

        Recent conversation context:
        {conversation_context}

        Return 1/2/3 thoughtful follow-up questions in a paragraph without numbering or bullet points."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Current user message: '{user_message}' | Generate empathetic follow-up questions for someone feeling {emotion} at urgency level {urgency_level}/5.")
            ]
            
            response = self.llm.invoke(messages)
            questions_text = response.content.strip()
            
            # Split into individual questions and clean them
            questions = [
                question.strip() 
                for question in questions_text.split('\n') 
                if question.strip() and '?' in question
            ]
            
            # Ensure we have 2-3 questions
            if len(questions) >= 2:
                return questions[:3]  # Return max 3 questions
            else:
                # Return empty list if LLM response is inadequate
                return []
                
        except Exception as e:
            pass

    def _generate_suggestions(self, emotion: str, urgency_level: int, user_message: str = "", user_name: str = "friend") -> List[str]:
        """Generate helpful suggestions using LLM based on detected emotion, urgency, and context."""
        
        system_prompt = f"""You are a mental health support assistant generating personalized coping suggestions. Based on the user's emotional state and situation, provide 3 practical, actionable suggestions that are:

        1. Immediately helpful for their current emotional state
        2. Appropriate for their urgency level (1-5 scale)
        3. Realistic and achievable in the next few hours
        4. Supportive without being overwhelming

        CONTEXT:
        - User's emotion: {emotion}
        - Urgency level: {urgency_level}/5 (1=casual, 2=mild concern, 3=moderate distress, 4=high distress, 5=crisis)
        - User's name: {user_name}

        URGENCY GUIDELINES:
        - Level 1-2: Gentle self-care and wellness suggestions
        - Level 3: More focused coping strategies and support-seeking
        - Level 4-5: Immediate safety measures and professional help

        SUGGESTION RULES:
        - Each suggestion should be 1-2 sentences max
        - Be specific and actionable ("Call your best friend" not "reach out to someone")
        - Match the urgency level appropriately
        - Consider their emotional state (anxious needs different help than depressed)
        - Avoid generic advice - make it feel personal

        Return EXACTLY 3 suggestions, one per line, without numbering or bullet points."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"User's emotional state: {emotion} (urgency {urgency_level}/5). Generate 3 personalized suggestions for immediate support. User message context: '{user_message}'")
            ]
            
            response = self.llm.invoke(messages)
            suggestions_text = response.content.strip()
            
            # Split into individual suggestions and clean them
            suggestions = [
                suggestion.strip() 
                for suggestion in suggestions_text.split('\n') 
                if suggestion.strip()
            ]
            
            # Ensure we have exactly 3 suggestions
            if len(suggestions) >= 3:
                return suggestions[:3]
            else:
                # Return empty list if LLM response is inadequate
                return []
                
        except Exception as e:
            return []

    def _generate_error_response_with_llm(self, message: str, emotion: str, urgency_level: int, user_name: str) -> str:
        """Generate contextual error response using LLM."""
        name = user_name or "friend"
        
        system_prompt = f"""You are MyBro, a caring friend who just experienced a technical issue while trying to respond. Generate a warm, apologetic message that:

        1. Acknowledges you're having technical trouble
        2. Shows you still care and are present for them
        3. Reassures them this doesn't diminish your support
        4. Gently encourages them to continue sharing
        5. Matches the appropriate tone for their emotional state

        CONTEXT:
        - User's name: {name}
        - Their emotional state: {emotion}
        - Urgency level: {urgency_level}/5
        - What they said: "{message}"

        TONE GUIDELINES:
        - If urgency is high (4-5): Show extra concern and urgency about staying connected
        - If urgency is moderate (3): Be caring and reassuring about technical issues
        - If urgency is low (1-2): Be friendly and casual about the hiccup

        Keep it personal, warm, and focused on maintaining the supportive connection despite technical issues."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Generate an error response for {name} who is feeling {emotion} (urgency {urgency_level}/5)")
            ]
            
            response = self.llm.invoke(messages)
            return response.content.strip()
            
        except:
            # Critical fallback for error situations
            return f"I'm having trouble processing that right now, {name}, but I'm still here for you. Can you tell me more about how you're feeling?"

    def _generate_error_suggestions_with_llm(self, message: str, emotion: str) -> List[str]:
        """Generate helpful suggestions for when there's a technical error."""
        system_prompt = f"""Generate 3 helpful suggestions for someone when you've experienced a technical issue. These should:

        1. Help them continue the conversation despite the technical problem
        2. Be encouraging and supportive
        3. Suggest alternative ways to express themselves
        4. Be appropriate for their emotional state: {emotion}

        Return exactly 3 suggestions, one per line, without numbering."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Generate error recovery suggestions for someone feeling {emotion} who said: '{message}'")
            ]
            
            response = self.llm.invoke(messages)
            suggestions_text = response.content.strip()
            
            suggestions = [s.strip() for s in suggestions_text.split('\n') if s.strip()]
            return suggestions[:3] if len(suggestions) >= 3 else [
                "Try rephrasing your message",
                "Tell me about your day", 
                "Share what's on your mind right now"
            ]
            
        except:
            return [
                "Try rephrasing your message",
                "Tell me about your day",
                "Share what's on your mind right now"
            ]

    def _generate_error_follow_up_questions_with_llm(self, user_name: str) -> List[str]:
        """Generate follow-up questions for error situations."""
        name = user_name or "friend"
        
        system_prompt = f"""Generate 2 caring follow-up questions for when you've had a technical issue. These should:

        1. Show continued care and interest
        2. Help restart the conversation smoothly
        3. Be warm and encouraging
        4. Use their name naturally: {name}

        Return exactly 2 questions, one per line."""

        try:
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=f"Generate error recovery questions for {name}")
            ]
            
            response = self.llm.invoke(messages)
            questions_text = response.content.strip()
            
            questions = [q.strip() for q in questions_text.split('\n') if q.strip() and '?' in q]
            return questions[:2] if len(questions) >= 2 else [
                "How are you feeling right now?",
                "What's been on your mind today?"
            ]
            
        except:
            return [
                "How are you feeling right now?",
                "What's been on your mind today?"
            ]

    def _generate_daily_summary_if_needed(self, email: str) -> Optional[dict]:
        """Generate summary for the user's last conversation day if needed."""
        from firebase_manager import firebase_manager
        from datetime import date
        
        # Find the last day user had a conversation
        last_conversation_date = firebase_manager.get_last_conversation_date(email)
        
        if last_conversation_date:
            date_str = last_conversation_date.strftime('%Y%m%d')
            today_str = date.today().strftime('%Y%m%d')
            
            # Only generate if it's not today and summary doesn't exist
            if date_str != today_str and not firebase_manager.daily_summary_exists(email, date_str):
                conversation_data = firebase_manager.get_conversation_by_date(email, date_str)
                
                if conversation_data and len(conversation_data.get('messages', {})) > 0:
                    # Generate summary for the last conversation day
                    summary = self._generate_conversation_summary(email, conversation_data, last_conversation_date)
                    if summary:
                        firebase_manager.store_daily_summary(email, date_str, summary)
                        return summary
        
        return None

    def _generate_conversation_summary(self, email: str, conversation_data: dict, conversation_date) -> Optional[dict]:
        """Generate AI summary of a day's conversation using LLM."""
        
        # Build conversation text
        messages = conversation_data.get('messages', {})
        conversation_text = ""
        emotions = []
        urgency_levels = []
        
        for msg_data in messages.values():
            role = msg_data.get('role', 'unknown')
            content = msg_data.get('content', '')
            emotion = msg_data.get('emotionDetected')
            urgency = msg_data.get('urgencyLevel', 1)
            
            conversation_text += f"{role}: {content}\n"
            if emotion:
                emotions.append(emotion)
            urgency_levels.append(urgency)
        
        if not conversation_text.strip():
            return None
        
        # Generate summary using LLM
        summary_prompt = f"""Summarize this conversation between a user and their mental health support friend:

CONVERSATION:
{conversation_text}

Create a friendly summary that covers:
1. What the user talked about and how they were feeling
2. Main topics or concerns they shared
3. Any positive moments or progress they mentioned
4. Important things to remember for next time you chat
5. How they seemed to be feeling by the end

Keep it:
- Simple and conversational (like notes a friend would take)
- Under 120 words
- Focused on what matters for continuing the friendship
- Written like "User talked about..." or "They seemed..."
- Remember this is for helping continue supportive conversations

Write a natural summary that helps remember what happened in this chat."""

        try:
            messages = [
                SystemMessage(content="You are a caring friend creating simple conversation summaries to help remember what you talked about with someone. Write in a natural, friendly tone like you're taking notes to remember for next time."),
                HumanMessage(content=summary_prompt)
            ]
            
            response = self.llm.invoke(messages)
            summary_text = response.content.strip()
            
            return {
                "date": conversation_date.strftime('%Y-%m-%d'),
                "summary_text": summary_text,
                "emotion_trend": list(set(emotions)) if emotions else [],
                "avg_urgency": sum(urgency_levels) / len(urgency_levels) if urgency_levels else 1,
                "message_count": len(messages)
            }
            
        except Exception as e:
            # Silently handle summary generation errors - not critical for chat functionality
            return None