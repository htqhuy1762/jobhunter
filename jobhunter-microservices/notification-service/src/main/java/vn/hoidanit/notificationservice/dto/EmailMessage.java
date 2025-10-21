package vn.hoidanit.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {
    private String to;
    private String subject;
    private String template;
    private Map<String, Object> variables;
    private boolean isHtml;
}

