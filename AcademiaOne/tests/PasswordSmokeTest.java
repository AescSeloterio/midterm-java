import java.lang.reflect.Method;
import javax.swing.SwingUtilities;
public class PasswordSmokeTest {
  public static void main(String[] args) throws Exception {
    LoginApp app = new LoginApp();
    Method hash = LoginApp.class.getDeclaredMethod("securePasswordHash",String.class);
    Method verify = LoginApp.class.getDeclaredMethod("verifyPassword",String.class,String.class);
    hash.setAccessible(true);verify.setAccessible(true);
    String one=(String)hash.invoke(app,"correct horse battery staple");
    String two=(String)hash.invoke(app,"correct horse battery staple");
    if(one.equals(two))throw new AssertionError("PBKDF2 salt should be random");
    if(!(Boolean)verify.invoke(app,"correct horse battery staple",one))throw new AssertionError("Valid password failed");
    if((Boolean)verify.invoke(app,"incorrect",one))throw new AssertionError("Wrong password accepted");
    if(!one.startsWith("pbkdf2$"))throw new AssertionError("Unexpected hash format");
    app.dispose();
    System.out.println("PASS: salted password hashing and verification");
  }
}
