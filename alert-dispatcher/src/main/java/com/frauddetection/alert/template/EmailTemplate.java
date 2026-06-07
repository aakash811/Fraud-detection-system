package com.frauddetection.alert.template;

import com.frauddetection.common.event.DecisionEvent;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {

    public String buildAlertBody(DecisionEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Fraud Detection Alert</h2>");
        sb.append("<table>");
        sb.append("<tr><td>Action:</td><td>").append(event.getAction()).append("</td></tr>");
        sb.append("<tr><td>Card:</td><td>****").append(event.getCardLast4()).append("</td></tr>");
        sb.append("<tr><td>Score:</td><td>").append(String.format("%.2f", event.getScore())).append("</td></tr>");
        sb.append("<tr><td>Reason:</td><td>").append(event.getReason()).append("</td></tr>");
        sb.append("<tr><td>Triggered Rules:</td><td>");
        event.getTriggeredRules().forEach(rule -> 
            sb.append("<br>").append(rule.ruleName()).append(": ").append(String.format("%.2f", rule.score()))
        );
        sb.append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }
}