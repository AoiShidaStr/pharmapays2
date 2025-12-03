
package services;

import dao.UserDAO;
import models.User;

public class UserService {
    private final UserDAO dao = new UserDAO();

    public User authentifier(String username, String password) {
        User u = dao.findByUsername(username);
        if (u!=null && u.getPassword().equals(password)) return u;
        return null;
    }

    public boolean inscrire(String username, String password, String role) {
        return dao.save(new User(username,password,role));
    }

    public boolean hasProof(String username) {
        // placeholder - always false for demo
        return false;
    }
}
