package in.zeta.qa.utils.hmac;

import lombok.Data;

@Data
public class Hmac {
    private String uuid;
    private String hmac;
    public long dueByTime;
}
