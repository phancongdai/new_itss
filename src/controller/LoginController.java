package controller;

import entity.user.User;
import views.screen.login_and_sign_up.alertMessage;

import java.sql.SQLException;

public class LoginController extends  BaseController{
    public User loginWithUsernameAndPassword(String username, String password) throws SQLException {
        User user = User.checkValidPassword(username, password);
        if (user == null) {
            new alertMessage().successMessage("HI");
            System.out.println("Incorrect username or password");
            return null;
        } else {
            System.out.println("Login successful");
            return user;
        }
    }
}