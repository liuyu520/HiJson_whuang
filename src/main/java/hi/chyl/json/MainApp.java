package hi.chyl.json;

import com.swing.messagebox.GUIUtil23;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import java.io.IOException;
import java.util.EventObject;

public class MainApp extends SingleFrameApplication {
    MainView3 m;

    public static MainApp getApplication() {
        return (MainApp) Application.getInstance(MainApp.class);
    }

	  /*protected void configureWindow(Window root)
      {
		  root.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("closing....");
				try {
					m.saveConfig();
				} catch (IOException e1) {
					e1.printStackTrace();
					GUIUtil23.errorDialog(e1);
				}
				super.windowClosing(e);
			}

		});
	  }*/

    public static void main(String[] args) {
        launch(MainApp.class, args);
    }

    protected void startup() {
        m = new MainView3(this);
        show(m);
        m.dealToolBar();
        m.dealTextArea();
        m.dealMenuBar();
        try {
            m.readConfig();
        } catch (IOException e2) {
            e2.printStackTrace();
            GUIUtil23.errorDialog(e2);
        }
	    /*m.getFrame().addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("closing....");
				try {
					m.saveConfig();
				} catch (IOException e1) {
					e1.printStackTrace();
					GUIUtil23.errorDialog(e1);
				}
				super.windowClosing(e);
			}

		});*/
        m.getApplication().addExitListener(new ExitListener() {

            @Override
            public void willExit(EventObject arg0) {
                try {
                    m.saveConfig();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    GUIUtil23.errorDialog(e1);
                }
            }

            @Override
            public boolean canExit(EventObject arg0) {
                return true;
            }
        });
    }
}
