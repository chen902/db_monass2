import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.swing.plaf.nimbus.State;
import java.sql.*;

public class Main extends Application
{
//    public static String CONN_STRING = "jdbc:sqlserver://i-mssql-01.informatik.hs-ulm.de;databaseName=kratzer_db;integratedSecurity=true";
//    public static String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static String CONN_STRING = "jdbc:mysql://localhost/kratzer_db?user=chen&password=12345678";
    public static String DRIVER = "com.mysql.cj.jdbc.Driver";

    private MapCanvas canvas;
    private MapSelectionPane selectionPane;
    private Stage stage;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        Connection conn = null;

        try
        {
            // initialize SQL server JDBC driver
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(CONN_STRING);
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT MAX(PosX), MAX(PosY) FROM CITY");
            rs.next();

            double width = rs.getDouble(1);
            double height = rs.getDouble(2);

            this.canvas = new MapCanvas(width,height);
        }
        catch(ClassNotFoundException e)
        {
            new Alert(Alert.AlertType.ERROR, "Error loading JDBC driver!", ButtonType.CLOSE).showAndWait();
            Platform.exit();
        }
        catch(SQLException e)
        {
            new Alert(Alert.AlertType.ERROR, "Error executing query!", ButtonType.CLOSE).showAndWait();
            Platform.exit();
        }
        finally
        {
            try
            {
                if(conn != null)
                    conn.close();
            }
            catch(SQLException e) {}
        }

        this.stage = primaryStage;
        this.stage.setTitle("Map viewer");
        this.selectionPane = new MapSelectionPane(this.canvas);

        HBox root = new HBox(10);
        root.setPadding(new Insets(10,10,10,10));
        root.getChildren().addAll(this.selectionPane,this.canvas);

        Scene scene = new Scene(root,root.getWidth(),root.getHeight());
        this.stage.setScene(scene);
        this.stage.setResizable(false);
        this.stage.show();
    }

    @Override
    public void stop() throws Exception
    {
        // attempt to close all database components in canvas
        this.canvas.destroy();
        super.stop();
    }
}
