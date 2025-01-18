package org.gdgoc.server.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gdgoc.server.domain.Profile;
import org.gdgoc.server.domain.User;
import org.gdgoc.server.exception.BusinessException;
import org.gdgoc.server.repository.ProfileRepository;
import org.gdgoc.server.repository.UserRepository;
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
    private final ObjectMapper objectMapper;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Autowired
    public RetirementCounselingService(ChatClient.Builder chatClientBuilder,
                                       ConversationStateRepository stateRepository,
                                       UserResponseRepository userResponseRepository,
                                       ProfileRepository profileRepository,
                                       UserRepository userRepository) {
        this.chatClient = chatClientBuilder.build();
        this.stateRepository = stateRepository;
        this.userResponseRepository = userResponseRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
        stateRepository.save(state);
        if (state.isComplete()) {
            return "모든 질문에 대한 응답을 완료하셨습니다";
//            return generateFinalReport(state);
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
                당신의 목적: ‘시니어 퇴직자가 은퇴 이후의 새 삶을 살 수 있도록, 자기 삶을 돌아보고, 자신이 지닌 가치를 깨닫게 하는 것’입니다.
                다음의 단계를 따라주세요.
                - 5가지 핵심 테마(나, 일, 흥미와 관심사, 나만의 경험, 메시지)를 바탕으로 구성된 1번부터 10번까지의 ’상위 질문‘이 있습니다.
                - 각 상위 질문에는 상대방의 추가적인 인사이트를 유도하는 하위 질문이 있습니다.
                - 다음 번호의 상위 질문으로 넘어가기 위해서는 각 상위 질문의 하위 질문을 모두 충족해야 합니다.
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

    public void makeProfile(String userId) throws JsonProcessingException {
        ConversationState state =  stateRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("INTERNAL_ERROR", "잘못된 요청입니다."));

        List<UserResponse> userResponses =  state.getResponses();
        List<String> histories = userResponses.stream().map(userResponse -> {
            StringBuilder sb = new StringBuilder();
            sb.append("질문: " + userResponse.getQuestion());
            sb.append("\n");
            sb.append("응답" + userResponse.getResponse());
            return sb.toString();
        }).toList();

        String history = toString().join("\n", histories);

        log.info(history);

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(getSystemProfilePrompt()),
                new UserMessage(history)
        ));
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String message  = response.getResult().getOutput().getContent();
        log.info("API Response: " + message);

        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new BusinessException("INTERNAL_ERROR", "잘못된 요청입니다."));
        ProfileResponse profileResponse = objectMapper.readValue(message, ProfileResponse.class);
        Profile profile = Profile.from(user, profileResponse);
        profileRepository.save(profile);
    }

    private String getSystemProfilePrompt() {
        return """
                인터뷰 대상자의 답변을 토대로, 그들의 고유한 가치와 전문성, 경험을 진정성 있게 전달하는 에세이를 다음 항목별로 작성해주세요. 각 항목에서는 구체적인 사례와 함께 대상자의 목소리가 생생하게 드러나도록 작성합니다:
                [나는 누구인가]
                                
                인생의 핵심 가치관과 신념
                현재 추구하고 있는 목표나 도전
                타인과의 관계 속에서 본인이 가진 강점과 역할
                                
                [나만의 전문성]
                                
                주요 경력과 전문 분야에서의 성과
                직무 수행 과정에서 가장 기억에 남는 프로젝트/경험
                본인의 전문성이 조직과 사회에 기여한 방식
                                
                [흥미와 관심사]
                                
                전문 영역에서 쌓은 역량을 확장/발전시키고자 하는 방향
                자기계발을 위해 현재 기울이고 있는 노력
                새로운 도전을 위한 학습과 준비 과정
                                
                [나만의 경험]
                                
                경력 과정에서 겪은 의미 있는 성공과 실패의 순간
                위기나 도전을 극복한 구체적인 사례
                타인과의 협업을 통해 이룬 성과와 배운 점
                                
                [메시지]
                                
                자신의 경험을 통해 전하고 싶은 핵심 메시지
                현재 분야에서 일하는 후배들에게 전하는 조언
                앞으로 이루고자 하는 변화나 영향력
               
                응답은 반드시 다음 형식을 지켜주세요:
                {
                  "slogan": "경청과 공감으로 문제를 해결하고 변화를 이끄는 소통 전문가",
                  "aboutMe": "존중과 상호 이해를 인생의 가장 중요한 가치로 삼고 있습니다. 공감의 첫걸음은 경청이라는 원칙과 솔직한 표현이 관계를 단단히 만든다는 신념으로 소통합니다. 다른 사람들은 주로 소통, 갈등 해결, 인간관계에 대해 제 조언을 구하는데, 이때 저는 상대방의 이야기를 충분히 듣고 그들의 감정을 이해한 후에 답변합니다. 단순히 해결책을 제시하기보다는 이 상황에서 가장 중요한 것이 무엇이라고 생각하세요?와 같은 질문을 통해 스스로 해결책을 찾도록 돕습니다.",
                  "work": "마케팅 회사에서 시장 조사와 광고 캠페인 기획으로 경력을 시작했고, 이후 중대형 기업에서 프로젝트 관리와 전략 기획을 담당했습니다. 가장 기억에 남는 것은 중소기업 브랜드 리뉴얼 프로젝트입니다. 기존 고객들의 감성을 유지하면서도 젊은 세대에게 어필해야 하는 과제 속에서, 마케팅팀은 전통적 이미지를, 디자인팀은 현대적 변화를 원했습니다. 각 팀의 목표와 우선순위를 정리하며 모두가 동의할 수 있는 방향을 찾아 성공적으로 프로젝트를 완수했습니다.", 
                  "interest": "문제 해결 능력을 실제 경험을 통해 발전시켜 왔습니다. 처음에는 막막했지만, 점차 작은 단위로 나누고 우선순위를 정하는 방식이 효과적이라는 것을 깨달았습니다. 관련 서적과 강의를 통해 배우고, 선배들의 피드백을 받아들이며 더 빠르고 효율적으로 문제를 해결할 수 있게 되었습니다.",
                  "experience": "가장 뿌듯했던 순간은 회사의 큰 갈등이 있었을 때입니다. 직원들 간의 오해와 갈등으로 생산성이 저하되었을 때, 각자의 이야기를 충분히 듣고 서로를 이해하도록 도왔습니다. 감정적 반응을 자제하도록 유도하여 논리적이고 차분한 대화를 이끌었고, 요약을 통해 서로의 감정을 이해했음을 확인했습니다. 결국 모두가 수용할 수 있는 해결책을 찾았고, 이후 팀은 더욱 단합된 모습으로 성과를 냈습니다.",
                  "message": "상대방의 이야기를 끝까지 듣고, 그 사람의 감정을 이해하려는 노력이 진정한 소통을 이끌어낸다고 생각합니다. 감정을 숨기지 않고 솔직하게 소통하는 것이 신뢰를 쌓는 방법이며, 이러한 신뢰를 바탕으로 할 때 지속 가능한 변화가 가능합니다."
                }
                """;
    }

    @Data
    public static class ProfileResponse {
        private String slogan;
        private String aboutMe;
        private String work;
        private String interest;
        private String experience;
        private String message;
    }
}
