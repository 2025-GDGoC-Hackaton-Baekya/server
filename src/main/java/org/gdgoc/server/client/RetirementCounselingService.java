package org.gdgoc.server.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RetirementCounselingService {
    private final ChatClient chatClient;
    private final ConversationStateRepository stateRepository;
    private final UserResponseRepository userResponseRepository;

    @Autowired
    public RetirementCounselingService(ChatClient.Builder chatClientBuilder,
                                       ConversationStateRepository stateRepository,
                                       UserResponseRepository userResponseRepository) {
        this.chatClient = chatClientBuilder.build();
        this.stateRepository = stateRepository;
        this.userResponseRepository = userResponseRepository;
    }

    public static final Map<String, List<String>> THEMES = Map.of(
            "나", List.of("당신은 어떤 사람인가요?", "다른 사람들이 자주 나에게 도움이나 조언을 구하는 주제는 무엇인가요?"),
            "일", List.of("지금까지 어떤 일을 해왔나요?", "남들보다 특별히 잘하거나 자신이 있는 일이 무엇인가요?"),
            "흥미와 관심사", List.of("자신의 전공 분야나 학력, 깊게 공부한 주제가 있나요?", "어떤 취미와 관심사를 가지고 있나요?"),
            "나만의 경험", List.of("내 인생에서 가장 큰 도전이나, 뿌듯했던 순간이 있나요?", "남들에게는 없는 나만의 경험은 무엇인가요?"),
            "메시지", List.of("내가 살아오면서 얻은 교훈 중 다른 사람들에게 도움이 될 만한 것은 무엇인가요?",
                    "내 이야기를 들은 사람들이 감동하거나 깨달음을 얻었던 순간은 언제인가요?")
    );

    public String processUserMessage(String userId, String message) {
        ConversationState state = stateRepository.findByUserId(userId)
                .orElse(new ConversationState(userId));

        if (message.equals("다음 질문으로 넘어가기")) {
            return moveToNextQuestion(state);
        }

        return generateFollowUpQuestion(state, message);
    }

    private String moveToNextQuestion(ConversationState state) {
        state.incrementCurrentQuestion();
        log.info("테마: " + state.getCurrentThemeIndex() + "질문: "  + state.getCurrentQuestionIndex());
        stateRepository.save(state);
        if (state.isComplete()) {
            return generateFinalReport(state);
        }
        return getCurrentMainQuestion(state);
    }

    private String generateFollowUpQuestion(ConversationState state, String userMessage) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(getSystemPrompt()),
                new UserMessage(userMessage)
        ));

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

        List<UserResponse> userResponses = state.getResponses();
        if (userResponses.size() > 0) {
            UserResponse userResponse = userResponses.get(userResponses.size() - 1);
            userResponse.setResponse(userMessage);
            userResponseRepository.save(userResponse);
        }

        String question = response.getResult().getOutput().getContent();
        state.addUserResponse(question, null);
        stateRepository.save(state);

        return question;
    }

    private String getSystemPrompt() {
        return """
                당신은 은퇴자들의 새로운 삶을 돕는 상담가입니다.
                사용자의 답변을 경청하고, 그들의 경험과 가치를 깊이 이해하여
                추가적인 통찰을 이끌어내는 적절한 후속 질문을 해주세요.
                질문은 공감적이고 구체적이어야 합니다.
                """;
    }

    private String getCurrentMainQuestion(ConversationState state) {
        int themeIndex = state.getCurrentThemeIndex();
        int questionIndex = state.getCurrentQuestionIndex();

        String theme = new ArrayList<>(THEMES.keySet()).get(themeIndex);
        return THEMES.get(theme).get(questionIndex);
    }

    private String generateFinalReport(ConversationState state) {
        List<UserResponse> responses = state.getResponses();
        StringBuilder report = new StringBuilder("=== 상담 결과 보고서 ===\n\n");

        THEMES.forEach((theme, questions) -> {
            report.append("【").append(theme).append("】\n");
            if (responses != null) {
                responses.forEach(response ->
                        report.append("• ").append(response).append("\n"));
            }
            report.append("\n");
        });

        return report.toString();
    }
}
