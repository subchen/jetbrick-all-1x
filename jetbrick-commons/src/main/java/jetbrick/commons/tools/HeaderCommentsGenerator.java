/**
 * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 * Email: subchen@gmail.com
 * URL: http://subchen.github.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrick.commons.tools;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class HeaderCommentsGenerator {
    private static int count = 0;
    //@formatter:off
    private static String DEFAULT_HEADER_COMMENT =
            "/**\n"
          + " * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.\n"
          + " * \n"
          + " * Email: subchen@gmail.com\n"
          + " * URL: http://subchen.github.io/\n"
          + " * \n"
          + " * Licensed under the Apache License, Version 2.0 (the \"License\");\n"
          + " * you may not use this file except in compliance with the License.\n"
          + " * You may obtain a copy of the License at\n"
          + " * \n"
          + " *   http://www.apache.org/licenses/LICENSE-2.0\n"
          + " * \n"
          + " * Unless required by applicable law or agreed to in writing, software\n"
          + " * distributed under the License is distributed on an \"AS IS\" BASIS,\n"
          + " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
          + " * See the License for the specific language governing permissions and\n"
          + " * limitations under the License.\n"
          + " */";
    //@formatter:on
    public static void main(String[] args) {
        final JFrame frame = new JFrame("HeaderCommentsGenerator 2.0 - subchen@gmail.com");
        JPanel panel = (JPanel) frame.getContentPane();

        // main
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(mainPanel, BorderLayout.CENTER);

        // main-top
        JPanel topPanel = new JPanel(new BorderLayout());
        final JTextField txtPath = new JTextField("Please select your file or path.");
        JButton btnSelectPath = new JButton("Browser...");
        topPanel.add(txtPath, BorderLayout.CENTER);
        topPanel.add(btnSelectPath, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // main-textarea
        final JTextArea txtComments = new JTextArea();
        txtComments.setText(DEFAULT_HEADER_COMMENT);
        mainPanel.add(new JScrollPane(txtComments), BorderLayout.CENTER);

        // bottom
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        final JTextField txtFileExt = new JTextField(".java", 5);
        final JTextField txtFileCharset = new JTextField("utf-8", 5);
        final JCheckBox chkFileUnixLineStyle = new JCheckBox("Unix Line", true);
        final JCheckBox chkAppendNewLine = new JCheckBox("New line before EOF", true);
        JButton btnOK = new JButton("Generate!");
        JButton btnClose = new JButton("Close");
        buttonPane.add(txtFileExt);
        buttonPane.add(txtFileCharset);
        buttonPane.add(chkFileUnixLineStyle);
        buttonPane.add(chkAppendNewLine);
        buttonPane.add(btnOK);
        buttonPane.add(btnClose);
        panel.add(buttonPane, BorderLayout.SOUTH);

        // button bind event
        btnSelectPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = chooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    txtPath.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        btnSelectPath.setMnemonic('B');

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = txtPath.getText();
                File file = new File(path);
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(frame, "Path '" + path + "' not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    count = 0;
                    doCommentFile(file, txtFileExt.getText(), txtFileCharset.getText(), chkFileUnixLineStyle.isSelected(), chkAppendNewLine.isSelected(), txtComments.getText());
                    JOptionPane.showMessageDialog(frame, "Finish, total " + count + " files are processed.", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        btnOK.setMnemonic('G');

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btnClose.setMnemonic('C');

        // show frame
        frame.setSize(574, 300);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
    }

    @SuppressWarnings("resource")
    private static void doCommentFile(File path, String fileExt, String encoding, boolean unixLine, boolean newlineBeforeEOF, String comments) {
        if (path != null && path.exists()) {
            if (path.isDirectory()) {
                String[] children = path.list();
                for (int i = 0; i < children.length; i++) {
                    File child = new File(path.getPath() + System.getProperty("file.separator") + children[i]);
                    doCommentFile(child, fileExt, encoding, unixLine, newlineBeforeEOF, comments);
                }
            } else {
                if (path.getName().equals("package-info.java")) {
                    return;
                }
                if (path.getName().toLowerCase().endsWith(fileExt)) {
                    System.out.println(path.getName());
                    count++;
                    try {
                        RandomAccessFile raFile = new RandomAccessFile(path, "rw");
                        byte[] content = new byte[(int) raFile.length()];
                        raFile.readFully(content);
                        String all = new String(content, encoding);
                        all = all.trim();
                        if (".java".equals(fileExt)) {
                            if (all.indexOf("package") != -1) {
                                all = all.substring(all.indexOf("package"));
                            } else if (all.indexOf("import") != -1) {
                                all = all.substring(all.indexOf("import"));
                            }
                        } else if (".jsp".equals(fileExt)) {
                            if (all.indexOf("<%@") != -1) {
                                all = all.substring(all.indexOf("<%@"));
                            } else if (all.indexOf("<!DOCTYPE") != -1) {
                                all = all.substring(all.indexOf("<!DOCTYPE"));
                            }
                        } else {
                            throw new RuntimeException("Invalid fileExt for lookup header comments.");
                        }
                        all = comments.trim() + (unixLine ? "\n" : "\r\n") + all;

                        if (newlineBeforeEOF) {
                            all = all + (unixLine ? "\n" : "\r\n");
                        }
                        if (unixLine) {
                            all.replaceAll("\r?\n", "\n");
                        } else {
                            all.replaceAll("\r?\n", "\r\n");
                        }

                        raFile.close();
                        FileWriter writer = new FileWriter(path);
                        writer.write(all);
                        writer.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
