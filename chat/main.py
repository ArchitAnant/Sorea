import sys
import os
sys.path.append(os.getcwd())

from chatbot import MentalHealthChatbot
from memory import MemoryManager
from firebase_manager import FirebaseManager
import json
from datetime import datetime

# Initialize all components
firebase_manager = FirebaseManager()
memory_manager = MemoryManager()
chatbot = MentalHealthChatbot()




def android_chat(user_prompt, user_email="arientific@gmail.com"):
    try:
        user_profile = memory_manager.get_user_profile(user_email)
        user_name = user_profile.name 
        chatbot._generate_daily_summary_if_needed(user_email)
        proactive_greeting = chatbot._generate_proactive_greeting(user_email)
        topic_filter = chatbot.health_filter.is_mental_health_related(user_prompt)
        emotion, urgency_level = chatbot.health_filter.detect_emotion(user_prompt)
        
        if not topic_filter.is_mental_health_related:
            redirect_response = "Sorry but i can not answer to that question!!!."
        
            firebase_manager.add_chat_pair(
                email=user_email,
                user_message=user_prompt,
                model_response=redirect_response,
                emotion_detected=emotion,  
                urgency_level=urgency_level  
            )
            
            return redirect_response


        chatbot._detect_important_events(user_prompt, user_email)
        context = memory_manager.get_conversation_context(user_email)
        recent_messages = memory_manager.get_recent_messages(user_email, 20)
        conversation_depth = len(recent_messages) if recent_messages else 0
        
        if urgency_level >= 5:
            crisis_response = chatbot._handle_crisis_situation(user_prompt, user_name)
            
            firebase_manager.add_chat_pair(
                email=user_email,
                user_message=user_prompt,
                model_response=crisis_response.message,
                emotion_detected=emotion,
                urgency_level=urgency_level
            )
            
            return crisis_response.message
        
        conversation_history = chatbot._build_conversation_history(user_email)
        enhanced_prompt = f"""{chatbot.system_prompt}

        CONVERSATION CONTEXT:
        {context}

        {f"PROACTIVE GREETING: You should start your response with this caring follow-up: '{proactive_greeting}'" if proactive_greeting else ""}

        CURRENT USER STATE:
        - Detected emotion: {emotion}
        - Urgency level: {urgency_level}/5
        - User prefers to be called: {user_name}
        - Conversation depth: {conversation_depth} messages

        🎯 RESPONSE GUIDANCE BASED ON URGENCY LEVEL:
        Level 1-2 (Casual/Mild): Be supportive but relaxed. Don't overreact. Match their energy level.
        Level 3 (Moderate): Show more concern and support. Ask deeper questions but stay calm.
        Level 4-5 (Crisis): NOW use your passionate, protective mode. Fight for them!

        🤗 CONVERSATION DEPTH GUIDANCE:
        - First 1-2 exchanges: Keep it general, build rapport
        - 3-5 exchanges: Start exploring their situation more
        - 6+ exchanges with emotional content: NOW you can ask about sleep, food, family, relationships naturally

        Remember to:
        1. Address them by their preferred name: {user_name}
        2. Reference relevant past conversations
        3. Match your tone to their ACTUAL emotional state
        4. Only escalate intensity if urgency level is high
        5. If there's a proactive greeting above, start with that
        """
        
        from langchain_core.messages import SystemMessage, HumanMessage
        messages = [SystemMessage(content=enhanced_prompt)]
        for msg in conversation_history[-10:]: 
            messages.append(msg)

        messages.append(HumanMessage(content=user_prompt))
        response = chatbot.llm.invoke(messages)
        bot_message = response.content
        try:
            follow_up_questions = chatbot._generate_follow_up_questions(emotion, urgency_level, user_name, user_email, user_prompt)
            suggestions = chatbot._generate_suggestions(emotion, urgency_level, user_prompt, user_name)
        except:
            follow_up_questions = []
            suggestions = []
        
        # Step 13: Mark events as followed up if proactive greeting was used
        if proactive_greeting:
            for event_type in ['exam', 'interview', 'appointment']:
                if event_type in proactive_greeting.lower():
                    chatbot._mark_event_followed_up(user_email, event_type)
                    break
        
        firebase_manager.add_chat_pair(
            email=user_email,
            user_message=user_prompt,
            model_response=bot_message,
            emotion_detected=emotion,
            urgency_level=urgency_level
        )
        
        return bot_message
        
    except Exception as e:
        return f"Sorry, I'm having technical difficulties. Please try again later. Error: {e}"

