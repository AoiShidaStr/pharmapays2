
package utils;

import models.User;

public class SessionManager {
    private static User currentUser;
    public static void setCurrentUser(User u){ currentUser = u; }
    public static User getCurrentUser(){ return currentUser; }
    public static void clear(){ currentUser = null; }
}
