package org.gdgoc.server.client;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConversationState {
    @Id
    private String userId;
    private int currentThemeIndex;
    private int currentQuestionIndex;

    public ConversationState(String userId) {
        this.userId = userId;
    }

    @OneToMany(mappedBy = "conversationState", cascade = CascadeType.ALL)
    private List<UserResponse> responses = new ArrayList<>();

    public void incrementCurrentQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex >= 2) {
            currentQuestionIndex = 0;
            currentThemeIndex++;
        }
    }

    public boolean isComplete() {
        return currentThemeIndex >= 5;
    }

    public void addUserResponse(String question, String response) {
        String currentTheme = new ArrayList<>(RetirementCounselingService.THEMES.keySet())
                .get(currentThemeIndex);
        responses.add(new UserResponse(this, currentTheme, question,response));
    }
}
