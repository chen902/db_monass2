import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application
{
//    public static String CONN_STRING = "jdbc:sqlserver://i-mssql-01.informatik.hs-ulm.de;databaseName=kratzer_db;integratedSecurity=true";
//    public static String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static String CONN_STRING = "jdbc:mysql://localhost/kratzer_db?user=chen&password=12345678";
    public static String DRIVER = "com.mysql.cj.jdbc.Driver";

    private MapCanvas canvas;
    private MapSelectionPane selectionPane;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Map viewer");

        this.canvas = new MapCanvas();
        this.selectionPane = new MapSelectionPane(this.canvas);

        HBox root = new HBox(10);
        root.setPadding(new Insets(10,10,10,10));
        root.getChildren().addAll(this.selectionPane,this.canvas);

        Scene scene = new Scene(root,root.getMinWidth(),root.getMinHeight());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        // attempt to close all database components in canvas
        this.canvas.destroy();
        super.stop();
    }
}
