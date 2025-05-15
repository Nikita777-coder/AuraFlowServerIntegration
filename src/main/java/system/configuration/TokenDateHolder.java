package system.configuration;

import org.springframework.stereotype.Component;

@Component
public class TokenDateHolder {
    private static final ThreadLocal<String> tokenDate = new ThreadLocal<>();

    public void set(String date) {
        tokenDate.set(date);
    }

    public String get() {
        return tokenDate.get();
    }

    public void clear() {
        tokenDate.remove();
    }
}

