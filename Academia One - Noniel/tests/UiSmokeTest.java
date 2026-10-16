import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;
public class UiSmokeTest {
  public static void main(String[] args) throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      try {
        LoginApp app = new LoginApp();
        Timer t = new Timer(600, e -> {
          JDialog d = null;
          for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing()) d=(JDialog)w;
          if (d==null) throw new AssertionError("Registration dialog missing");
          int[] count={0};
          check(d,count);
          if(count[0]!=2) throw new AssertionError("Expected two footer buttons, found "+count[0]);
          System.out.println("PASS: registration opens with two visible footer buttons");
          d.dispose(); app.dispose();
        }); t.setRepeats(false); t.start();
        Method m=LoginApp.class.getDeclaredMethod("openRegistration");m.setAccessible(true);m.invoke(app);
      } catch(Exception ex){throw new RuntimeException(ex);}
    });
  }
  static void check(Container c,int[] count){for(Component x:c.getComponents()){if(x instanceof JButton b && (b.getText().equalsIgnoreCase("Cancel")||b.getText().equalsIgnoreCase("CREATE ACCOUNT"))){if(!b.isShowing()||b.getWidth()<100)throw new AssertionError("Button clipped: "+b.getText());count[0]++;}if(x instanceof Container)check((Container)x,count);}}
}
