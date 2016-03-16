package hi.chyl.json.listen;

import com.JSON_java.JSONObject;
import com.JSON_java.XML;
import com.common.util.SystemHWUtil;
import com.io.hw.json.HWJacksonUtils;
import com.string.widget.util.RegexUtil;
import com.string.widget.util.ValueWidget;
import com.swing.component.ComponentUtil;
import com.swing.component.TextCompUtil2;
import com.swing.dialog.toast.ToastMessage;
import com.swing.menu.MenuUtil2;
import hi.chyl.json.MainView3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ReplaceMenuActionListener implements ActionListener {
    private JTextArea ta;
    private MainView3 mainView3;

    public ReplaceMenuActionListener(JTextArea ta) {
        super();
        this.ta = ta;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println(command);

        if (command.equalsIgnoreCase("CRLF2Blank")) {
            String content = this.ta.getText();
            if (!ValueWidget.isNullOrEmpty(content)) {
                content = SystemHWUtil.CRLF2Blank(content);
                this.ta.setText(content);
            }
        } else if (command.equalsIgnoreCase("ChinaQuotes2BEnglishQuotes")) {//中文引号替换成为英文
            String content = this.ta.getText();
            if (!ValueWidget.isNullOrEmpty(content)) {
                content = ValueWidget.replaceChinaSingleQuotes(ValueWidget.replaceChinaQuotes(content));
                this.ta.setText(content);
            }
        } else if (command.equalsIgnoreCase("unQuotesEscape")) {//反转义英文双引号
            String content = this.ta.getText();
            if (!ValueWidget.isNullOrEmpty(content)) {
                content = content.replace("\\\"", SystemHWUtil.ENGLISH_QUOTES);
                this.ta.setText(content);
            }
        } else if (command.equalsIgnoreCase("quotesEscape")) {//转义英文双引号
            String content = this.ta.getText();
            if (!ValueWidget.isNullOrEmpty(content)) {
                content = content.replace(SystemHWUtil.ENGLISH_QUOTES, "\\\"");
                this.ta.setText(content);
            }
        } else if (command.equalsIgnoreCase("china2English")) {//反转义英文双引号
            String content = this.ta.getText();
            if (!ValueWidget.isNullOrEmpty(content)) {
//				content=content.replaceAll("[\u4E00-\u9FA5]*", "aa");
                content = RegexUtil.replaceChinese(content, "a");
                this.ta.setText(content);
            }
        } else if (command.equalsIgnoreCase("xml2json")) {//反转义英文双引号
            String content = this.ta.getText();
            if (!ValueWidget.isNullOrEmpty(content)) {
                JSONObject xmlJSONObj = XML.toJSONObject(content);
                String jsonPrettyPrintString = xmlJSONObj.toString(4);
                if (ValueWidget.isNullOrEmpty(jsonPrettyPrintString)
                        || jsonPrettyPrintString.equals("{}")) {
                    return;
                }
                int result = JOptionPane
                        .showConfirmDialog(
                                null,
                                "<html>"
                                        + "Are you sure to convert <font color=\"blue\"  style=\"font-weight:bold;\">xml</font> to <font color=\"red\"  style=\"font-weight:bold;\">"
                                        + "json</font> ?</html>",
                                "确认提示", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    this.ta.setText(jsonPrettyPrintString);
                }
            }
        } else if (command.equals(MenuUtil2.ACTION_STR2UPPER_CASE)) {
            String text = this.ta.getText();
            if (text != null) {
                text = text.toUpperCase();
                this.ta.setText(text);
            }
        } else if (command.equals(MenuUtil2.ACTION_STR2LOWER_CASE)) {
            String text = this.ta.getText();
            if (text != null) {
                text = text.toLowerCase();
                this.ta.setText(text);
            }
        } else if (command.equals("unQuotesJson")) {
            String text = this.ta.getText();
            if (text != null) {
                text = RegexUtil.getJsonFromQuotes(text, false);
                this.ta.setText(text);
            }
        } else if (command.equals("export jpg")) {
            ComponentUtil.chooseDestFile(ta, "jpg", false);
        } else if (command.equals("export png")) {
            ComponentUtil.chooseDestFile(ta, "png", false);
        } else if (command.equals("export jpg Specify")) {
            ComponentUtil.chooseDestFile(ta, "jpg", true);
        } else if (command.equals("export png Specify")) {
            ComponentUtil.chooseDestFile(ta, "png", true);
        } else if (command.equals(MenuUtil2.ACTION_IMAGE_COPY)) {//复制图片到剪切板
            String content = this.ta.getText();
            if (ValueWidget.isNullOrEmpty(content)) {
                ToastMessage.toast("无内容,不会复制", 3000, Color.red);
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ComponentUtil.copyImage(ta, false);
                }
            }).start();
        } else if (command.equals(MenuUtil2.ACTION_IMAGE_COPY_SPECIFY_HEIGHT)) {//复制图片到剪切板
            String content = this.ta.getText();
            if (ValueWidget.isNullOrEmpty(content)) {
                ToastMessage.toast("无内容,不会复制", 3000, Color.red);
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ComponentUtil.copyImage(ta, true);
                }
            }).start();
        } else if (command.equals("删除两边的双引号")) {
            String content = this.ta.getText();
            if (!ValueWidget.isNullOrEmpty(content)) {
                content = RegexUtil.deleteTwoQuote(content);
                this.ta.setText(content);
            }
        } else if (command.startsWith(MenuUtil2.ACTION_QUERY_STRING2JSON)) {
            String content = this.ta.getText();
            if (ValueWidget.isNullOrEmpty(content)) {
                return;
            }
            Map requestMap = new HashMap();
            SystemHWUtil.setArgumentMap(requestMap, content, true, null, null);
            String jsonResult = HWJacksonUtils.getJsonP(requestMap);
            if (!ValueWidget.isNullOrEmpty(jsonResult)) {
                this.ta.setText(jsonResult);
            }
        } else if (command.equals("删除{前面的所有内容")) {
            String content = this.ta.getText();
            if (!ValueWidget.isNullOrEmpty(content)) {
                content = RegexUtil.deleteEveryThingBeforeBrace(content);
                this.ta.setText(content);
            }
        } else if (command.equals("打开日志文件")) {
            if (this.mainView3 != null) {
                this.mainView3.openLoggerFile2();
            }
        } else if (command.equals(MenuUtil2.ACTION_IMAGE_COPY_SPECIFY_WIDTH_HEIGHT)) {
            TextCompUtil2.copyImgAction(this.ta);
        }
    }

    public MainView3 getMainView3() {
        return mainView3;
    }

    public void setMainView3(MainView3 mainView3) {
        this.mainView3 = mainView3;
    }


}
