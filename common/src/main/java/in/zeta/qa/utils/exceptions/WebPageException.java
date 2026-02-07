package in.zeta.qa.utils.exceptions;

/**
 * @author charankumarh@zeta.qa
 * @implNote Throw the exception when you wanted to notify the problem occurred while accessing
 * Any Web Page across the framework and its corresponding operations.
 */

public class WebPageException extends RuntimeException {

    public WebPageException(String errorMessage) {
        super(errorMessage);
    }
}
