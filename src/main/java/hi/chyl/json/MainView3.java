package hi.chyl.json;

import com.cmd.dos.hw.util.CMDUtil;
import com.common.util.SystemHWUtil;
import com.common.util.WindowUtil;
import com.io.hw.file.util.FileUtils;
import com.string.widget.util.ValueWidget;
import com.swing.dialog.toast.ToastMessage;
import com.swing.menu.MenuUtil2;
import com.swing.messagebox.GUIUtil23;
import com.time.util.TimeHWUtil;
import hi.chyl.json.listen.MyMenuActionListener;
import hi.chyl.json.listen.ReplaceMenuActionListener;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/***
 * 双击Shift 去掉两边的双引号<br>
 * 双击Ctrl,转义双引号<br>
 * 双击ESC 可以使文本域在只读和可编辑之间切换
 *
 * @author huangweii
 *         2015年7月11日
 */
public class MainView3 extends MainView {
    /***
     * 日志文件路径(不是log4j)
     */
    public static final String logFilePath = System.getProperty("user.home") + File.separator + ".log_hijson.log";
    private ResourceMap resourceMap;
    private File configFile = new File(System.getProperty("user.home") + File.separator + ".hijson_content.txt");
    private File logFile;

    public MainView3(SingleFrameApplication app) {
        super(app);
        this.resourceMap = ((MainApp) Application.getInstance(MainApp.class)).getContext().getResourceMap(MainView3.class);
        this.getFrame().setTitle("json 格式化(优化 by 黄威 qq:1287789687)");
        logFile = new File(logFilePath);
    }

    /***
     * 转义双引号
     *
     * @param area2
     * @param isIntelligence : 是否智能判断,true时,不对两边的双引号进行转义
     */
    public void quotesEscape(JTextArea area2, boolean isIntelligence) {
        String selectContent = area2.getSelectedText();
        if (selectContent == null || selectContent.equals("")) {
            GUIUtil23.warningDialog("请先选中文本");
            return;
        }
        String quotes = SystemHWUtil.ENGLISH_QUOTES;
        if (!selectContent.contains(quotes)) {//压根选中的文本没有双引号
            return;
        }
        //首尾均有双引号
        boolean surroundQuotes = selectContent.endsWith(quotes) && selectContent.startsWith(quotes);
        if (isIntelligence && surroundQuotes) {//被双引号包围
            int length = selectContent.length();
            selectContent = selectContent.substring(1, length - 1);
        }
        String content = selectContent.replace(SystemHWUtil.ENGLISH_QUOTES, "\\\"");
        if (isIntelligence && surroundQuotes) {
            content = quotes + content + quotes;
        }
        area2.replaceSelection(content);
    }

    public void dealToolBar() {
        final JTextArea area2 = getTextArea();
        JToolBar toolbar = getToolBar();
        JButton btnDelAll = new JButton("删除全部");
        JButton btnPasteAfterDel = new JButton(MenuUtil2.ACTION_STR_PASTE_AFTER_DELETE);
        JButton btnQuotesEscape = new JButton(MenuUtil2.ACTION_DOUBLE_QUOTES_ESCAPE);//双引号转义
        JButton btnQuotesEscapeIntelligence = new JButton("智能双引号转义");
        JButton replaceChinaQuotes = new JButton("替换中文双引号");
        JButton replaceChinaSingleQuotes = new JButton("替换中文单引号");
        JButton deleteCRLF = new JButton("删除换行");
        JButton copyAll = new JButton("复制全部");
        copyAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = area2.getText();
                if (ValueWidget.isNullOrEmpty(content)) {
                    return;
                }
                WindowUtil.setSysClipboardText(content);
            }
        });
        deleteCRLF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = area2.getText();
                if (ValueWidget.isNullOrEmpty(content)) {
                    return;
                }
                content = SystemHWUtil.deleteAllCRLF(content);
                area2.setText(content);
            }
        });
        replaceChinaQuotes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = area2.getText();
                content = ValueWidget.replaceChinaQuotes(content);
                area2.setText(content);
            }
        });
        //替换中文单引号为英文双引号
        replaceChinaSingleQuotes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = area2.getText();
                content = ValueWidget.replaceChinaSingleQuotes(content);
                area2.setText(content);
            }
        });
        btnQuotesEscape.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quotesEscape(area2, false);
            }
        });
        btnQuotesEscapeIntelligence.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quotesEscape(area2, true);
            }
        });
        toolbar.add(btnPasteAfterDel);
        toolbar.add(btnDelAll);
        toolbar.add(btnQuotesEscape);
        toolbar.add(btnQuotesEscapeIntelligence);
        toolbar.add(replaceChinaQuotes);
        toolbar.add(deleteCRLF);
        toolbar.add(copyAll);
        btnDelAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getTextArea().setText(SystemHWUtil.EMPTY);
                getTextArea().requestFocus();
            }
        });
        //删除后黏贴
        btnPasteAfterDel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = WindowUtil.getSysClipboardText();
                if (ValueWidget.isNullOrEmpty(content)) {
                    return;
                }
                getTextArea().setText(content);
                getTextArea().requestFocus();
                appendStr2LogFile(content);

            }
        });
    }

    public void dealTextArea() {
        final JTextArea ta = getTextArea();
        KeyListener[] keyListeners = ta.getKeyListeners();
        ta.addKeyListener(new KeyAdapter() {
            private long lastTimeMillSencond;
            private long lastTimeMillSencondCtrl;
            private long lastTimeMillSencondEsc;

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A/*全选*/
                        || e.getKeyCode() == KeyEvent.VK_C/* 复制 */
                        || e.getKeyCode() == KeyEvent.VK_F/*格式化*/
                        || e.getKeyCode() == KeyEvent.VK_Z/*剪切*/
                        || e.getKeyCode() == KeyEvent.VK_V) {
//						System.out.println(e.getKeyCode());
                    lastTimeMillSencondCtrl = 0;
                }
                if (e.isShiftDown()) {
                    if (lastTimeMillSencond == 0) {
                        lastTimeMillSencond = System.currentTimeMillis();
                    } else {
                        long currentTime = System.currentTimeMillis();
                        long delta = currentTime - lastTimeMillSencond;
                        if (MenuUtil2.isDoubleClick(delta)) {
//									System.out.println("双击Shift");
                            lastTimeMillSencond = 0;
                            String selectContent = ta.getSelectedText();
                            if (ValueWidget.isNullOrEmpty(selectContent)) {
                                return;
                            }
                            selectContent = SystemHWUtil.deleteQuotes(selectContent);
                            ta.replaceSelection(selectContent);
                        } else {
                            lastTimeMillSencond = System.currentTimeMillis();
                        }
                    }
                } else if (e.isControlDown() && (e.getKeyCode() != KeyEvent.VK_V/*86 */ && e.getKeyCode() != KeyEvent.VK_Z/*90*/
                        && e.getKeyCode() != KeyEvent.VK_C/*67*/ && e.getKeyCode() != KeyEvent.VK_A/*65*/)) {//双击Ctrl
//						System.out.println(e.getKeyCode());
//						System.out.println("lastTimeMillSencondCtrl:"+lastTimeMillSencondCtrl);
                    if (lastTimeMillSencondCtrl == 0) {
                        lastTimeMillSencondCtrl = System.currentTimeMillis();
                    } else {
                        long currentTime = System.currentTimeMillis();
                        long delta = currentTime - lastTimeMillSencondCtrl;
//							System.out.println(lastTimeMillSencondCtrl+" "+currentTime+" "+delta);
                        if (MenuUtil2.isDoubleClick(delta)) {
                            System.out.println("双击Ctrl");
                            lastTimeMillSencondCtrl = 0;
                            String selectContent = ta.getSelectedText();
                            if (ValueWidget.isNullOrEmpty(selectContent)) {
                                return;
                            }
                            quotesEscape(ta, false);
                        } else {
                            lastTimeMillSencondCtrl = System.currentTimeMillis();
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (lastTimeMillSencondEsc == 0) {
                        lastTimeMillSencondEsc = System.currentTimeMillis();
                    } else {
                        long currentTime = System.currentTimeMillis();
                        if (MenuUtil2.isDoubleClick(currentTime - lastTimeMillSencondEsc)) {
//									System.out.println("双击Esc");
                            lastTimeMillSencondEsc = 0;
                            String content = ta.getText();
                            if (ValueWidget.isNullOrEmpty(content)) {
                                return;
                            }
                            boolean isEditable = ta.isEditable();
                            ta.setEditable(!isEditable);
                        } else {
                            lastTimeMillSencondEsc = System.currentTimeMillis();
                        }
                    }
                }

            }
        });
        ta.requestFocus();
    }

    public void dealMenuBar() {
        final JTextArea ta = getTextArea();
        JMenuBar menuBar = getMenuBar();
        JMenuItem menu2 = new JMenuItem(MenuUtil2.ACTION_DOUBLE_QUOTES_ESCAPE);
        /* menu2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("mouseClicked bbb");
				final JTextArea area2=getTextArea();				
				quotesEscape(area2,true);
				
			}
		});
		 menu2.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("addKeyListener ccc");
				final JTextArea area2=getTextArea();				
				quotesEscape(area2,true);
				
			}
		});*/
        menu2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("addActionListener aaa");
                final JTextArea area2 = getTextArea();
                quotesEscape(area2, true);
            }
        });


        JMenu menuConvert = new JMenu("转化");
        JMenuItem date2MillSecond = new JMenuItem("时间转换为毫秒");
        date2MillSecond.setActionCommand("toMillSecond");
        JMenuItem millSecond2date = new JMenuItem("毫秒转换为时间");
        millSecond2date.setActionCommand("MillSecondto");
        JMenuItem date2Second = new JMenuItem("时间转换为秒");
        date2Second.setActionCommand("toSecond");

        JMenuItem unicodeDecode = new JMenuItem("Unicode解码");

        MyMenuActionListener myMenuActionListener = new MyMenuActionListener(ta);
        date2MillSecond.addActionListener(myMenuActionListener);
        millSecond2date.addActionListener(myMenuActionListener);
        date2Second.addActionListener(myMenuActionListener);
        menuConvert.add(date2MillSecond);
        menuConvert.add(millSecond2date);
        menuConvert.add(date2Second);
        unicodeDecode.addActionListener(myMenuActionListener);
        menuConvert.add(unicodeDecode);
        menuBar.add(menuConvert);

        JMenu menuReplace = new JMenu("替换");

        JMenuItem cRLF2Blank = new JMenuItem("换行->空格");
        cRLF2Blank.setActionCommand("CRLF2Blank");
        JMenuItem chinaQuotes2EnglishQuotes = new JMenuItem("中文引号->英文双引号");
        chinaQuotes2EnglishQuotes.setActionCommand("ChinaQuotes2BEnglishQuotes");
        JMenuItem unQuotesEscape = new JMenuItem("\\\" -> \"");
        unQuotesEscape.setActionCommand("unQuotesEscape");
        JMenuItem quotesEscape = new JMenuItem("\" -> \\\"");
        quotesEscape.setActionCommand("quotesEscape");

        JMenuItem optimizationJsonM = new JMenuItem("\" -> \\\"");
        optimizationJsonM.setActionCommand("optimizationJson");

        
        JMenuItem unQuotesJson = new JMenuItem("\"{}\" -> {}");
        unQuotesJson.setActionCommand("unQuotesJson");

        JMenuItem xml2json = new JMenuItem("xml -> json");
        xml2json.setActionCommand("xml2json");
        JMenuItem china2English = new JMenuItem("中文 -> 英文");
        china2English.setActionCommand("china2English");

        JMenuItem upperCaseM = new JMenuItem(
                MenuUtil2.ACTION_STR2UPPER_CASE);

        JMenuItem lowerCaseM = new JMenuItem(
                MenuUtil2.ACTION_STR2LOWER_CASE);

        JMenuItem deleteTwoQuoteM = new JMenuItem(MenuUtil2.ACTION_DELETE_TWO_QUOTE);
        JMenuItem deleteEveryThingBeforeBraceM = new JMenuItem("删除{前面的所有内容");
        JMenuItem queryString2Json = new JMenuItem(MenuUtil2.ACTION_QUERY_STRING2JSON);
        queryString2Json.setActionCommand(MenuUtil2.ACTION_QUERY_STRING2JSON);

        menuReplace.add(cRLF2Blank);
        menuReplace.add(chinaQuotes2EnglishQuotes);
        menuReplace.add(unQuotesEscape);
        menuReplace.add(quotesEscape);
        menuReplace.add(optimizationJsonM);
        menuReplace.add(unQuotesJson);
        menuReplace.add(xml2json);
        menuReplace.add(china2English);
        menuReplace.add(upperCaseM);
        menuReplace.add(lowerCaseM);
        menuReplace.add(deleteTwoQuoteM);
        menuReplace.add(deleteEveryThingBeforeBraceM);
        menuReplace.add(queryString2Json);

        ReplaceMenuActionListener replaceMenuActionListener = new ReplaceMenuActionListener();
        replaceMenuActionListener.setMainView3(this);
        cRLF2Blank.addActionListener(replaceMenuActionListener);
        chinaQuotes2EnglishQuotes.addActionListener(replaceMenuActionListener);
        unQuotesEscape.addActionListener(replaceMenuActionListener);
        quotesEscape.addActionListener(replaceMenuActionListener);
        optimizationJsonM.addActionListener(replaceMenuActionListener);
        unQuotesJson.addActionListener(replaceMenuActionListener);
        xml2json.addActionListener(replaceMenuActionListener);
        china2English.addActionListener(replaceMenuActionListener);
        upperCaseM.addActionListener(replaceMenuActionListener);
        lowerCaseM.addActionListener(replaceMenuActionListener);
        queryString2Json.addActionListener(replaceMenuActionListener);

        deleteTwoQuoteM.addActionListener(replaceMenuActionListener);
        deleteEveryThingBeforeBraceM.addActionListener(replaceMenuActionListener);
        menuBar.add(menuReplace);

        JMenu menuExport = new JMenu("导出");

        JMenuItem exportJpg = new JMenuItem("导出jpg");
        exportJpg.setActionCommand("export jpg");
        exportJpg.addActionListener(replaceMenuActionListener);

        JMenuItem exportPng = new JMenuItem("导出png");
        exportPng.setActionCommand("export png");
        exportPng.addActionListener(replaceMenuActionListener);

        JMenuItem exportJpgSpecify = new JMenuItem("导出jpg 指定高度");
        exportJpgSpecify.setActionCommand("export jpg Specify");
        exportJpgSpecify.addActionListener(replaceMenuActionListener);

        JMenuItem exportPngSpecify = new JMenuItem("导出png 指定高度");
        exportPngSpecify.setActionCommand("export png Specify");
        exportPngSpecify.addActionListener(replaceMenuActionListener);

        JMenuItem copyImagePng = new JMenuItem(MenuUtil2.ACTION_IMAGE_COPY);
        copyImagePng.setActionCommand(MenuUtil2.ACTION_IMAGE_COPY);
        copyImagePng.addActionListener(replaceMenuActionListener);

        JMenuItem copyImageSpecifyheight = new JMenuItem(MenuUtil2.ACTION_IMAGE_COPY_SPECIFY_HEIGHT);
        copyImageSpecifyheight.setActionCommand(MenuUtil2.ACTION_IMAGE_COPY_SPECIFY_HEIGHT);
        copyImageSpecifyheight.addActionListener(replaceMenuActionListener);

        JMenuItem copyImageSpecifyWidthHeight = new JMenuItem(MenuUtil2.ACTION_IMAGE_COPY_SPECIFY_WIDTH_HEIGHT);
        copyImageSpecifyWidthHeight.setActionCommand(MenuUtil2.ACTION_IMAGE_COPY_SPECIFY_WIDTH_HEIGHT);
        copyImageSpecifyWidthHeight.addActionListener(replaceMenuActionListener);

        JMenuItem openLoggerM = new JMenuItem("打开日志文件");
        openLoggerM.addActionListener(replaceMenuActionListener);

        menuExport.add(exportJpg);
        menuExport.add(exportPng);
        menuExport.add(exportJpgSpecify);
        menuExport.add(exportPngSpecify);

        menuExport.add(copyImagePng);
        menuExport.add(copyImageSpecifyheight);
        menuExport.add(copyImageSpecifyWidthHeight);
        menuExport.add(openLoggerM);
        menuBar.add(menuExport);
        menuBar.add(menu2);
    }

    /****
     * 我通过反射获取的
     *
     * @return
     */
    public JToolBar createToolBar() {
        Class clazz = MainView.class;
        Object obj = null;
        Method m;
        try {
            m = clazz.getDeclaredMethod("createToolBar", new Class[]{});
            m.setAccessible(true);
            obj = m.invoke(this, null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return (JToolBar) obj;
    }

    /****
     * 我通过反射获取的
     *
     * @return
     */
    public JTextArea getTextArea() {
        Class clazz = MainView.class;
        Object obj = null;
        Method m;
        try {
            m = clazz.getDeclaredMethod("getTextArea", new Class[]{});
            m.setAccessible(true);
            obj = m.invoke(this, null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return (JTextArea) obj;
    }

    public void saveConfig() throws IOException {
        if (!configFile.exists()) {
            try {
                SystemHWUtil.createEmptyFile(configFile);
            } catch (IOException e) {
                e.printStackTrace();
                GUIUtil23.errorDialog(e);
            }
        }
        CMDUtil.show(configFile);//因为隐藏文件是只读的
        FileUtils.writeToFile(configFile, getTextArea().getText(), SystemHWUtil.CHARSET_UTF);
        CMDUtil.hide(configFile);
    }

    /***
     * 读取配置文件
     *
     * @throws IOException
     */
    public void readConfig() throws IOException {
        if (configFile.exists()) {
            InputStream inStream = new FileInputStream(configFile);
            String content = FileUtils.getFullContent2(inStream, SystemHWUtil.CHARSET_UTF, true);
//				inStream.close();//及时关闭资源
            getTextArea().setText(content);
        }
    }

    /***
     * 追加内容到日志
     *
     * @param content
     */
    public void appendStr2LogFile(final String content) {
        appendStr2LogFile(content, true);
    }

    /***
     * 追加内容到日志
     *
     * @param content
     */
    public void appendStr2LogFile(final String content, final boolean isCloseOutput) {
        if (!ValueWidget.isNullOrEmpty(content)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.appendStr2File(logFile, TimeHWUtil.getCurrentFormattedTime() + SystemHWUtil.CRLF, SystemHWUtil.CHARSET_UTF, false);
                        FileUtils.appendStr2File(logFile, content + SystemHWUtil.CRLF, SystemHWUtil.CHARSET_UTF, isCloseOutput);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    public void openLoggerFile2() {
        File logFile = new File(logFilePath);
        if (logFile.exists()) {
            FileUtils.open_file(logFile);
        } else {
            ToastMessage.toast("日志文件:" + logFilePath + "不存在", 3000, Color.red);
        }
    }
}
