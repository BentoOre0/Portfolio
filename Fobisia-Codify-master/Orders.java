/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.table.TableColumn;
import raven.cell.TableActionCellEditor;
import raven.cell.TableActionCellRender;
import raven.cell.TableActionEvent;
/**
 *
 * @author jerem
 */

public class Orders extends javax.swing.JFrame {
    
    //Define some checking functions
    public static boolean ASCIIandAlNumonly(String input) // check if only alphanumeric plus keyboard chars only
    {
        int a;
        for (int i = 0; i < input.length(); i++) 
        {
            if (Character.isDigit(input.charAt(i))){
                //numbers allowed
            } else {
                try{
                    a = (int)input.charAt(i);//change to ascii value, if error then return false
                } catch (Exception e2){
                    return false;
                }
                
            }
        }
        return true;
    }
    public static int charcount(String charlist, char elem) {
        String reduced = charlist;
        reduced = reduced.replace( String.valueOf(elem),"");
        return charlist.length()-reduced.length();
    }

    public static int[] occur(String charlist, char elem) {
        // used to get indexes of all periods. I will use to make sure that each period has atleast 2 characters between them
        int j = 0;
        String temp = "";
        for (int i = 0; i < charlist.length(); i++) {
            if (charlist.charAt(i) == elem) {
                temp += String.valueOf(i);
                temp += " ";
            }
        }
        String[] lines = temp.split(" ");
        int[] myindexes = new int[lines.length];
        for (int i = 0; i < myindexes.length; i++) {
            myindexes[i] = Integer.parseInt(lines[i]);
        }
        return myindexes;
    }

    public static boolean check_special_char(String charlist) {
        int asciival;
        for (int i = 0; i < charlist.length(); i++) {
            asciival = (int) charlist.charAt(i);
            if ((asciival >= 32 && asciival <= 47 && asciival != 46) || (asciival >= 58 && asciival <= 63 && asciival != 64) || (asciival >= 91 && asciival <= 96 && asciival != 95) || (asciival >= 123 && asciival <= 126)) {
                return true;// has a special character init with some exceptions ('@','.','_')
            }
        }
        return false;
    }
    public static boolean checkemail(String email) {
        if (!ASCIIandAlNumonly(email)){
            return false;
        } else if (charcount(email, '@') != 1) {
            return false;
        } else if (charcount(email.substring(email.lastIndexOf('@') + 1), '.') < 1) {
            return false;
        } else if (email.substring(email.lastIndexOf('@'), email.lastIndexOf('.')).length() < 2) {
            return false;
        } else if (email.substring(0, email.lastIndexOf('@')).length() < 3) {
            return false;
        } else if (charcount(email, ' ') > 0) {
            return false;
        } else if (check_special_char(email)) {
            return false;
        }
        int[] list = occur(email, '.');
        String sub;
        
        // make sure there is atleast 2 characters between each period
        for(int i = 0; i < list.length-1; i++) {
            sub = email.substring(list[i], list[i + 1]);
            if(sub.length() < 2){
                return false;
            }
        }
        return true;
    }
    
    
    // Will double check this later
    public static boolean doesTableExists(String tableName, Connection con) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        ResultSet result = meta.getTables(null, null, tableName.toUpperCase(), null);
       
        return result.next();
    }
    
    /**
     * Creates new form NewJFrame
     */
    public Orders() {
        initComponents();
        TableActionEvent event = new TableActionEvent() {
            @Override
            public void onEdit(int row){
                try {
                    DefaultTableModel model = (DefaultTableModel) tblTransactions.getModel();
//                    int row = tblClothes.getSelectedRow();
                    txtProdID.setText(model.getValueAt(row,1).toString());
                    txtName.setText(model.getValueAt(row, 2).toString());
                    txtEmail.setText(model.getValueAt(row, 3).toString());
                    txtCost.setText(model.getValueAt(row, 4).toString());
                    cmbStatus.setSelectedItem(model.getValueAt(row, 5));
                    
                    
                } catch (Exception E) {

                }

            }
            @Override
            public void onDelete(int row){
                if (tblTransactions.isEditing()) {
                    tblTransactions.getCellEditor().stopCellEditing();
                }
                try {
                    DefaultTableModel model = (DefaultTableModel) tblTransactions.getModel();
                    int rownum = tblTransactions.getSelectedRow();
                    if (rownum == -1) {
                        JOptionPane.showMessageDialog(null, "Please select a row");
                    } else {
                        int idremov = Integer.parseInt(model.getValueAt(rownum, 0).toString());
                        String databaseURL2 = "jdbc:derby:orders";
                        Connection con2 = DriverManager.getConnection(databaseURL2);
                        Statement st2 = con2.createStatement();

                        //original QUERY
                        String sql2 = "DELETE FROM Transactions WHERE tid = " + idremov + "";//                            String sql1="INSERT INTO students(sid, name, age, email) VALUES('"+txtId.getText()+"','"+txtName.getText()+"','"+cmbAge.getSelectedItem().toString()+"','"+txtEmail.getText()+"')";
                        st2.executeUpdate(sql2);
                        con2.close();
                        String databaseURL = "jdbc:derby:orders";

                        con2 = DriverManager.getConnection(databaseURL);
                        st2 = con2.createStatement();
                        sql2 = "select * from Transactions";
                        ResultSet rs = st2.executeQuery(sql2);

                        model.setRowCount(0);// clearing the table

                        while (rs.next() == true) // does something exist in the resultset
                        {
                            model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getDouble(7), rs.getString(8), rs.getString(9), rs.getDouble(10)}); // not 0 indexed find id, name, age and then email
                        }

                        //clear it all
                        txtName.setText("");
                        txtEmail.setText("");
                        txtCost.setText("");
                        txtProdID.setText("");
                        cmbStatus.setSelectedIndex(0);
                        con2.close();
                    }
                } catch (Exception E) {

                }                
                
            }
            @Override
            public void onView(int row){
                try {
                    String name = "";
                    String email = "";
                    String item = "";
                    String size = "";
                    //might use in future
                    String databaseURL1 = "jdbc:derby:inventory";
                    Connection con1 = DriverManager.getConnection(databaseURL1);
                    Statement st1 = con1.createStatement();
                    DefaultTableModel model = (DefaultTableModel) tblTransactions.getModel();
                    int rownum = tblTransactions.getSelectedRow();

                    int idview = Integer.parseInt(model.getValueAt(rownum, 1).toString());
                    String sql1 = "select item, size from clothes where sid = " + idview + "";
                    ResultSet rs = st1.executeQuery(sql1);
                    while (rs.next()) {
                        item = rs.getString(1);
                        size = rs.getString(2);
                    }
                    con1.close();
                    
                    String databaseURL2 = "jdbc:derby:orders";
                    Connection con2 = DriverManager.getConnection(databaseURL2);
                    Statement st2 = con2.createStatement();
                    int rownum2 = tblTransactions.getSelectedRow();
                    int tid = Integer.parseInt(model.getValueAt(rownum2, 0).toString()); // fetch tid
                    //tid--;
                    int prodid = Integer.parseInt(model.getValueAt(rownum2, 1).toString()); // fetch tidS
                    System.out.println(tid+" "+prodid);
                    String sql2 = "select pname, pemail from transactions where tid = " + tid + " and prodid = "+prodid+"";
                    ResultSet rs2 = st2.executeQuery(sql2);
                    while (rs2.next()){
                        name = rs2.getString(1);
                        email = rs2.getString(2);
                    }
                    
                    
                    String out = "Item : '"+item+"'\nSize: '"+size+"'\nPrevious owner name: '"+name+"'\nPrevious owner email: '"+email+"'";
                    JOptionPane.showMessageDialog(null, out);
                } catch (Exception E) {


                }
            } 
        };
        tblTransactions.getColumnModel().getColumn(8).setCellRenderer(new TableActionCellRender());
        tblTransactions.getColumnModel().getColumn(8).setCellEditor(new TableActionCellEditor(event));
        try{
            
            String databaseURL = "jdbc:derby:orders;create=true";
            Connection con = DriverManager.getConnection(databaseURL);
            Statement st = con.createStatement();
            //errors
            if (!doesTableExists("Transactions", con)) {
                //create table and fields
                String sql = "create table Transactions (tid int, prodid int, name varchar(256), email varchar(100), Pname varchar(256), Pemail varchar(256), cost double, status varchar(256), date varchar(256), profit double)";

                st.execute(sql);
            }

            
            con.close();
            DefaultTableModel model = (DefaultTableModel) tblTransactions.getModel();

            String databaseURL1 = "jdbc:derby:orders";
            Connection con1 = DriverManager.getConnection(databaseURL1);
            Statement st1 = con1.createStatement();

            String sql1="select * from Transactions";
            ResultSet rs= st1.executeQuery(sql1);

            //Populate Jtable

            model.setRowCount(0);// clearing the table

            while (rs.next() == true) // does something exist in the resultset
            {
                model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getDouble(7), rs.getString(8), rs.getString(9), rs.getDouble(10)}); // not 0 indexed find id, name, age and then email
            }
            
            con1.close();
            //
        } catch (Exception e){

        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        txtEmail = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        txtCost = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        cmbStatus = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtProdID = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTransactions = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        jPanel3.setBackground(new java.awt.Color(102, 153, 255));

        txtEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmailActionPerformed(evt);
            }
        });

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        txtCost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCostActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Name");

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Email");

        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Sell");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-SELECT-", "AVAILABLE", "RESERVED", "DAMAGED", "SOLD" }));

        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Status");

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("ProductID");

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("RETURN AND SELL ONLY");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(73, 73, 73)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(106, 106, 106)))
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(64, 64, 64))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(41, 61, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtProdID)
                                    .addComponent(txtCost, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                                    .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(18, 18, 18)
                                .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(13, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(jLabel3)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel2)
                .addGap(8, 8, 8)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addGap(4, 4, 4)
                .addComponent(txtProdID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnClear))
                .addGap(41, 41, 41)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReset)
                .addGap(114, 114, 114))
        );

        tblTransactions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Transaction No.", "ProductID", "Name", "Email", "Sell At", "Status", "Date", "Profit", "Action"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTransactions.setRowHeight(40);
        tblTransactions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblTransactions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTransactionsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblTransactions);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1118, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>                        

    private void tblTransactionsMouseClicked(java.awt.event.MouseEvent evt) {                                             
                
    }                                            

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
        try{
            DefaultTableModel model = (DefaultTableModel) tblTransactions.getModel();
            String databaseURL = "jdbc:derby:orders";
            Connection con = DriverManager.getConnection(databaseURL);
            Statement st = con.createStatement();
            String sql = "select * from Transactions";
            ResultSet rs = st.executeQuery(sql);

            model.setRowCount(0);// clearing the table

            while (rs.next() == true) // does something exist in the resultset
            {
                model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getDouble(7), rs.getString(8), rs.getString(9),rs.getDouble(10)}); // not 0 indexed find id, name, age and then email
            }
            con.close();

            //clear it all
            txtName.setText("");
            txtEmail.setText("");
            txtCost.setText("");
            txtProdID.setText("");
            cmbStatus.setSelectedIndex(0);
        } catch (Exception E){

        }

    }                                        

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
        try{
            DefaultTableModel model = (DefaultTableModel) tblTransactions.getModel();
            String databaseURL = "jdbc:derby:orders";
            Connection con = DriverManager.getConnection(databaseURL);
            Statement st = con.createStatement();

            //original QUERY

            String sql;

            try{
                int id = Integer.parseInt(txtSearch.getText());
                sql = "SELECT * FROM Transactions WHERE prodid = "+id+"";
            } catch (Exception isnotinteger){
                String searchvalue = "%"+txtSearch.getText()+"%"; //regex is dubs
                sql = "SELECT * FROM Transactions WHERE NAME LIKE '"+searchvalue+"' OR EMAIL LIKE '"+searchvalue+"' ORDER BY TID";
            }

            ResultSet rs = st.executeQuery(sql);
            model.setRowCount(0);// clearing the table

            
            while (rs.next() == true) // does something exist in the resultset
            {
                model.addRow(new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getDouble(7), rs.getString(8), rs.getString(9), rs.getDouble(10)}); // not 0 indexed find id, name, age and then email
            }
            con.close();

        } catch (Exception E){

        }

    }                                         

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
    }                                         

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
        try{
            txtName.setText("");
            txtEmail.setText("");
            txtCost.setText("");
            txtProdID.setText("");
            cmbStatus.setSelectedIndex(0);
        } catch (Exception E){

        }

    }                                        

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // TODO add your handling code here:
        try {
            //if has inside
            if (txtName.getText() == "" || txtEmail.getText() == "" || txtCost.getText() == "" ||cmbStatus.getSelectedIndex() == 0|| txtProdID.getText()=="") {
                JOptionPane.showMessageDialog(null, "PLEASE FILL IN THE FIELDS");
            } else {
                String Email = txtEmail.getText();
                if (checkemail(Email) == false) {
                    JOptionPane.showMessageDialog(null, "PLEASE FILL IN A VALID EMAIL FORMAT");
                } else {
                    // check if valid cost and id
                    try {
                        double cost = Double.parseDouble(txtCost.getText());
                        try{
                            int pid = Integer.parseInt(txtProdID.getText());
                            try {
                                String databaseURL = "jdbc:derby:inventory";
                                Connection con = DriverManager.getConnection(databaseURL);
                                Statement st = con.createStatement();
                                
                                String sql = "select sid, commission, cost, name, email from clothes where sid = " + pid + ""; // check if exists in table
                                ResultSet rs = st.executeQuery(sql);
                                
                                //create a function to check if said pid is already in the db
                                
                                if (rs.next()) { // does something exist in the resultset?
                                    // fetch item and size from previous database
                                    //Now, let's input into ORDERS db
                                    double com = rs.getDouble(2);
                                    double buycost = rs.getDouble(3);
                                    String pname = rs.getString(4);
                                    String pemail = rs.getString(5);
                                    int result = JOptionPane.showConfirmDialog(null, "Are you sure that ALL VALUES are correct? You will not be able to change them later. Financial Transactions will be made.","Confirmation", JOptionPane.YES_NO_OPTION);
                                    if (result == JOptionPane.YES_OPTION){
                                        //get maximum in orders db
                                        int max = 0;
                                        String databaseURL3 = "jdbc:derby:orders";

                                        Connection con3 = DriverManager.getConnection(databaseURL3);

                                        Statement st3 = con3.createStatement();

                                        String sql3 = "select tid from Transactions where prodid = "+pid+"";
                                        ResultSet rs3 = st3.executeQuery(sql3);

                                        while (rs3.next() == true) // does something exist in the resultset
                                        {
                                            if (rs3.getInt(1) > max) {
                                                max = rs3.getInt(1);
                                            }

                                        }

                                        con3.close();

                                        max++; // get maximum tid and increment to get current id

                                        //time to add to our jtable and orders db
                                        DefaultTableModel model = (DefaultTableModel) tblTransactions.getModel();

                                        
                                        LocalDate currentDate = LocalDate.now();
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                        String currentDateAsString = currentDate.format(formatter);
                                        
                                        double sellcost = Double.valueOf(txtCost.getText());
                                        double profit = sellcost-(buycost+com);
                                        if (profit < 0){
                                            JOptionPane.showMessageDialog(null, "Increase selling price or decrease commmision; you will lose money");
                                        } else {
                                            
                                            String databaseURL4 = "jdbc:derby:orders";


                                            Connection con4 = DriverManager.getConnection(databaseURL4);
                                            Statement st4 = con4.createStatement();
                                            String sql4 = "INSERT INTO Transactions(tid,prodid,name,email,pname,pemail,cost,status,date,profit) VALUES (" + max + "," + pid + ",'" + txtName.getText() + "','" + txtEmail.getText() + "','" + pname + "','" + pemail + "'," + Double.valueOf(txtCost.getText()) + ",'" + cmbStatus.getSelectedItem().toString() + "','" + currentDateAsString + "',"+profit+")";
                                            st4.execute(sql4);


                                            con4.close();

                                            //get previous email and previous name and calculate comissions for each person
                                            String databaseURL69 = "jdbc:derby:orders";

                                            Connection con69 = DriverManager.getConnection(databaseURL69);

                                            Statement st69 = con69.createStatement();

                                            String sql69 = "select * from Transactions where prodid = " + pid + " order by tid";
                                            
                                            //i did not store previous owner in db I do have the value but yet s

                                            ResultSet rs69 = st69.executeQuery(sql69);
                                            boolean runout = false;
                                            while (rs69.next() == true) {
                                                pname = rs69.getString(5);
                                                pemail = rs69.getString(6);
                                                int position = rs69.getInt(1);
                                                double finalcommission = (double)((com/max)*100.0)/100.0;

                                                if (finalcommission < 1){
                                                    runout = true;
                                                } else {
                                                    //ADD EMAIL FUNCTIONALITY HERE
                                                    System.out.println("'" + pname + "' aka '" + pemail + "' gets " + finalcommission + "");
                                                }
                                            }
                                            if (runout){
                                                JOptionPane.showMessageDialog(null, "Commission has run out.");
                                            }
                                            

                                            con69.close();// since order by ascending, most recent is last
                                            //so I can call pname and pemail

                                            //CAREFUL HEREE!!! PLS MAKE SURE YOU COPY PASTE THIS
                                            //update the inventory to show current owner and current date
                                            String databaseURL420 = "jdbc:derby:inventory";
                                            Connection con420 = DriverManager.getConnection(databaseURL420);
                                            Statement st420 = con420.createStatement();
                                            int idupdate = pid;
                                            //aaaa

                                            String sql420 = "UPDATE clothes SET name = '" + txtName.getText() + "', email = '" + txtEmail.getText() + "',status = '" + cmbStatus.getSelectedItem().toString() + "' WHERE sid = " + idupdate + "";
                                            st420.executeUpdate(sql420);
                                            con420.close();

                                            //repopulate jtable
                                            String databaseURL5 = "jdbc:derby:orders";
                                            Connection con5 = DriverManager.getConnection(databaseURL5);
                                            Statement st5 = con5.createStatement();
                                            String sql5 = "select * from Transactions";
                                            ResultSet rs5 = st5.executeQuery(sql5);

                                            model.setRowCount(0);// clearing the table

                                            while (rs5.next() == true) // does something exist in the resultset
                                            {
                                                model.addRow(new Object[]{rs5.getInt(1), rs5.getInt(2), rs5.getString(3), rs5.getString(4), rs5.getDouble(7), rs5.getString(8), rs5.getString(9), rs5.getDouble(10)}); // not 0 indexed find id, name, age and then email
                                            }
                                            con5.close();

                                            //clear it all
                                            txtName.setText("");
                                            txtEmail.setText("");
                                            txtCost.setText("");
                                            txtProdID.setText("");
                                            cmbStatus.setSelectedIndex(0);
                                        }
                                    } else {
                                        
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null, "THERE IS NO EXISTING PRODUCT ID IN THE INVENTORY");
                                }
                                con.close();
                            } catch (Exception dbe){

                                JOptionPane.showMessageDialog(null, "Inventory Error: Inventory could be empty or got corrupted");
                            }
                        } catch (Exception ie){
                            JOptionPane.showMessageDialog(null, "PLEASE FILL IN AN INTEGER PRODUCT ID.");
                        }
                    } catch (Exception de) {
                        JOptionPane.showMessageDialog(null, "PLEASE FILL IN A VALID COST. TRY REMOVING SYMBOLS.");
                    }
                }
            }
        } catch (Exception E){

        }
    }                                       

    private void txtCostActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // TODO add your handling code here:
    }                                       

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {                                        
        // TODO add your handling code here:
    }                                       

    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
    }                                        
    
//    private void tableColumnSize(){
//        tblClothes.getColumn
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Orders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Orders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Orders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Orders.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Orders().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblTransactions;
    private javax.swing.JTextField txtCost;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtProdID;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration                   
}
