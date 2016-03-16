package hi.chyl.json.listen;

import com.kunlunsoft.unicode2chinese.Conversion;
import com.string.widget.util.ValueWidget;
import com.swing.messagebox.GUIUtil23;
import com.time.util.TimeHWUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Date;

public class MyMenuActionListener implements ActionListener {
    private JTextArea ta;

    public MyMenuActionListener(JTextArea ta) {
        super();
        this.ta = ta;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println(command);
        String selectContent = this.ta.getSelectedText();
        if (selectContent == null || selectContent.equals("")) {
            return;
        }
        if (command.equalsIgnoreCase("toMillSecond")) {
            Date date;
            try {
                date = TimeHWUtil.getDate4Str(selectContent);
                selectContent = String.valueOf(date.getTime());
            } catch (ParseException e1) {
                e1.printStackTrace();
                GUIUtil23.errorDialog(e1);
            }

        } else if (command.equalsIgnoreCase("MillSecondto")) {
            if (ValueWidget.isInteger(selectContent)) {
                long time = Long.parseLong(selectContent);
                Date date = new Date(time);
                selectContent = TimeHWUtil.formatDateTime(date);
            }
        } else if (command.equalsIgnoreCase("toSecond")) {
            Date date;
            try {
                date = TimeHWUtil.getDate4Str(selectContent);
                selectContent = String.valueOf(date.getTime() / 1000);
            } catch (ParseException e1) {
                e1.printStackTrace();
                GUIUtil23.errorDialog(e1);
            }

        } else if (command.equalsIgnoreCase("Unicode解码")) {
            selectContent = Conversion.unicodeToChinese(selectContent);
        }

        this.ta.replaceSelection(selectContent);
    }
}
