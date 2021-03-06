package sample;

import bpnn.BPNN;
import bpnn.DataSet.DataGroup;
import bpnn.DataSet.DataSet;
import bpnn.function.ActivateFunction;
import bpnn.function.Sigmord;
import bpnn.function.Tanh;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.text.DecimalFormat;

public class Controller {
    @FXML
    Button initButton;
    @FXML
    TextField netStructText;
    @FXML
    TextArea trainSetTextArea;
    @FXML
    TextArea predictSetTextArea;
    @FXML
    TextField maxIterText;
    @FXML
    TextField minErrorText;
    @FXML
    TextField speedText;
    @FXML
    TextField momentumText;
    @FXML
    TabPane tabPane;
    @FXML
    AnchorPane errorLineChartPane;
    @FXML
    ComboBox activateChoose;
    @FXML
    TableView<TestData> compareTable;
    @FXML
    TableView<PredictData> predictTable;
    @FXML
    ComboBox testSelect;

    LineChart<Number,Number>lineChart;

    double[] X;

    @FXML
    public void initNet(){
        int maxIter=Integer.parseInt(maxIterText.getText());
        double minError=Double.parseDouble(minErrorText.getText());
        String netStructure=netStructText.getText();
        double speed=Double.parseDouble(speedText.getText());
        double momentum=Double.parseDouble(momentumText.getText());

        int funcN=activateChoose.getSelectionModel().getSelectedIndex();

        ActivateFunction activateFunction=null;
        switch (funcN){
            case 0:
                activateFunction=new Sigmord();
                break;
            case 1:
                activateFunction=new Tanh();
                break;
            default:
                activateFunction=new Tanh();
        }

        trainSetTextArea.setText("");
        predictSetTextArea.setText("");
        lineChart.getData().clear();

        Main.bpnn=new BPNN(maxIter,minError,netStructure,speed,momentum,activateFunction);

        compareTable.getColumns().clear();
        compareTable.getItems().clear();
        for(int i=0;i<Main.bpnn.xnumber;i++){
            TableColumn tc=new TableColumn("x"+i);
            int finalI = i;
            tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TestData,Double>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<TestData,Double> param) {
                    return param.getValue().doubleProperties[finalI];
                }
            });
            compareTable.getColumns().add(tc);

        }
        for(int i=0;i<Main.bpnn.ynumber;i++){
            TableColumn tc=new TableColumn("dy"+i);
            int finalI = i;
            tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TestData,Double>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<TestData,Double> param) {
                    return param.getValue().doubleProperties[2*finalI+Main.bpnn.xnumber];
                }
            });
            compareTable.getColumns().add(tc);
            TableColumn tc1=new TableColumn("oy"+i);
            int finalI1 = i;
            tc1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TestData,Double>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<TestData,Double> param) {
                    return param.getValue().doubleProperties[2*finalI1+1+Main.bpnn.ynumber];
                }
            });
            compareTable.getColumns().add(tc1);
        }

        predictTable.getColumns().clear();
        predictTable.getItems().clear();
        for(int i=0;i<Main.bpnn.xnumber;i++){
            TableColumn tc=new TableColumn("x"+i);
            int finalI = i;
            tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PredictData,Double>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<PredictData,Double> param) {
                    return param.getValue().doubleProperties[finalI];
                }
            });
            predictTable.getColumns().add(tc);

        }
        for(int i=0;i<Main.bpnn.ynumber;i++){
            TableColumn tc=new TableColumn("dy"+i);
            int finalI = i;
            tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PredictData,Double>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<PredictData,Double> param) {
                    return param.getValue().doubleProperties[2*finalI+Main.bpnn.xnumber];
                }
            });
            predictTable.getColumns().add(tc);

            TableColumn tc1=new TableColumn("oy"+i);
            int finalI1 = i;
            tc1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PredictData,Double>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<PredictData,Double> param) {
                    return param.getValue().doubleProperties[2*finalI1+1+Main.bpnn.xnumber];
                }
            });
            predictTable.getColumns().add(tc1);

            TableColumn tc2=new TableColumn("oy"+i+"-dy"+i);
            int finalI2 = i;
            tc2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PredictData,Double>, ObservableValue<Number>>() {
                @Override
                public ObservableValue<Number> call(TableColumn.CellDataFeatures<PredictData,Double> param) {
                    return param.getValue().doubleProperties[finalI1+Main.bpnn.xnumber+Main.bpnn.ynumber*2];
                }
            });
            predictTable.getColumns().add(tc2);
        }
    }


    @FXML
    public void chooseTrainSet(){
        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("选择训练数据集");
        File file=fileChooser.showOpenDialog(Main.primaryStage);
        train(file);
        beginDraw();
    }
    @FXML
    public void chooseDefaultSet(){
        File file=new File("res/train.txt");
        //File file=new File("train.txt");//for artifact
        train(file);
        File file1=new File("res/predict.txt");
        //File file1=new File("predict.txt");//for artifact
        predict(file1);
        beginDraw();
    }
    void train(File file){
        DataSet dataSet=new DataSet(Main.bpnn.xnumber,Main.bpnn.ynumber);
        String trainset="";
        try {
            BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line=null;
            while((line=reader.readLine())!=null){
                String[] ss=line.split("\t");
                DataGroup dataGroup=new DataGroup(dataSet.xn,dataSet.yn);
                for(int i=0;i<dataSet.xn;i++){
                    dataGroup.inputs[i]=Double.parseDouble(ss[i]);
                }
                for(int i=dataSet.xn;i<dataSet.xn+dataSet.yn;i++){
                    dataGroup.outputs[i-dataSet.xn]=Double.parseDouble(ss[i]);
                }
                dataSet.trainGroups.add(dataGroup);

                trainset+=line;
                trainset+="\n";
            }
            Main.bpnn.dataSet=dataSet;

            if(Main.bpnn.testrate>=dataSet.trainGroups.size()){
                Main.bpnn.testrate=0;
            }
            dataSet.sortTrainGroup();
            dataSet.generateTestGroups(Main.bpnn.testrate);
            dataSet.generateOne();//归一化

            trainSetTextArea.appendText(trainset);
            Main.bpnn.train(Main.bpnn.dataSet);

            if(Main.bpnn.testrate!=0){
                Main.bpnn.getpredictTest();
                DataSet ds=Main.bpnn.dataSet;
                compareTable.getItems().clear();
                for(int i=0;i<ds.oldtestGroups.size();i++){
                    TestData td=new TestData(dataSet,i);
                    compareTable.getItems().add(td);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void choosePredictSet(){
        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("选择训练数据集");
        File file=fileChooser.showOpenDialog(Main.primaryStage);
        //File file=new File("res/predict.txt");
        //File file=new File("predict.txt");//for artifact
        predict(file);

    }

    void predict(File file){
        tabPane.getSelectionModel().select(5);
        try {
            BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            //-
            String line=null;
            int linenumber=0;
            while((line=reader.readLine())!=null){
                String[] ss=line.split("\t");
                double[] X=new double[Main.bpnn.xnumber];
                double[] dY=new double[Main.bpnn.ynumber];
                for(int i=0;i<Main.bpnn.xnumber;i++){
                    X[i]=Double.parseDouble(ss[i]);
                }

                String xs="X["+linenumber+"]=";
                for(int i=0;i<Main.bpnn.xnumber;i++){
                    xs+=(ss[i]+"\t");
                }
                predictSetTextArea.appendText(xs+"\n");

                if(ss.length==Main.bpnn.xnumber+Main.bpnn.ynumber){
                    String dy="dY["+linenumber+"]=";
                    for(int i=0;i<Main.bpnn.ynumber;i++){
                        dy+=(ss[Main.bpnn.xnumber+i]+"\t");

                        dY[i]=Double.parseDouble(ss[Main.bpnn.xnumber+i]);
                    }
                    predictSetTextArea.appendText(dy+"\n");
                }

                double[] Y=Main.bpnn.predict(X);

                predictTable.getItems().add(new PredictData(X,dY,Y));
                String so="";
                for(int i=0;i<Y.length;i++){
                    so+=(Y[i]+"\t");
                }
                predictSetTextArea.appendText("oY["+linenumber+"]="+so+"\n");

                linenumber++;
            }
            predictSetTextArea.appendText("最终训练次数："+Main.bpnn.nmaxtrainnumber+"\n"+"最终误差："+Main.bpnn.trainError +"\n");
            //--

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void beginDraw(){
        tabPane.getSelectionModel().select(2);
        XYChart.Series series=new XYChart.Series();

        //series.getChart().set


//        //
//        Thread thread=new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for(int i=0;i<Main.bpnn.trainErrorlist.size();i++){
//                    series.getData().add(new XYChart.Data(i,Main.bpnn.trainErrorlist.get(i)));
//                    try {
//                        Thread.sleep(300);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        thread.start();
//        //
        for(int i = 0; i<Main.bpnn.trainErrorlist.size(); i++){
            series.getData().add(new XYChart.Data(i,Main.bpnn.trainErrorlist.get(i)));
        }
        series.setName("训练曲线--最终训练误差: "+Main.bpnn.trainErrorlist.get(Main.bpnn.trainErrorlist.size()-1));
        lineChart.getData().add(series);

        if(Main.bpnn.testrate!=0){
            XYChart.Series testSeries=new XYChart.Series();
            testSeries.setName("测试曲线--最终测试误差: "+Main.bpnn.testErrorlist.get(Main.bpnn.testErrorlist.size()-1));
            for(int i=0;i<Main.bpnn.testErrorlist.size();i++){
                testSeries.getData().add(new XYChart.Data(i,Main.bpnn.testErrorlist.get(i)));
            }
            lineChart.getData().add(testSeries);
        }
    }

    @FXML
    void initialize(){
        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "sigmord",
                        "tanh"
                );
        activateChoose.setItems(options);

        NumberAxis xAxis=new NumberAxis();
        NumberAxis yAxis=new NumberAxis();
        xAxis.setLabel("迭代次数");
        lineChart=new LineChart<Number, Number>(xAxis,yAxis);
        lineChart.setCreateSymbols(false);
        errorLineChartPane.getChildren().add(lineChart);

        ObservableList<String> options1 =
                FXCollections.observableArrayList(
                        "1/20",
                        "1/10",
                        "1/7",
                        "1/5",
                        "1/3"
                );
        testSelect.setItems(options1);
        testSelect.setOnAction((Event ev)->{
            int n=testSelect.getSelectionModel().getSelectedIndex();
            switch (n){
                case 0:
                    Main.bpnn.testrate=20;
                    break;
                case 1:
                    Main.bpnn.testrate=10;
                    break;
                case 2:
                    Main.bpnn.testrate=7;
                    break;
                case 3:
                    Main.bpnn.testrate=5;
                    break;
                case 4:
                    Main.bpnn.testrate=3;
                    break;
                default:
                    Main.bpnn.testrate=7;
            }
        });
        manager=new ScriptEngineManager();
        engine=manager.getEngineByName("nashorn");
        expressionText.setText("x0=uniform(1,10);\n" +
                "x1=uniform(2,8);\n" +
                "x2=gaussian(10,2);\n" +
                "y0=x0+x1;\n" +
                "y1=x0-x1;\n" +
                "y0+=Math.random()*2\n" +
                "y1+=Math.random()");
        tabPane.getSelectionModel().select(4);
    }
    class TestData{
        public SimpleDoubleProperty[] doubleProperties;

        public TestData(DataSet dataSet,int n) {
            this.doubleProperties= new SimpleDoubleProperty[dataSet.xn + 2 * dataSet.yn];
            for(int i=0;i<dataSet.xn;i++){
                doubleProperties[i]=new SimpleDoubleProperty();
                doubleProperties[i].set(dataSet.oldtestGroups.get(n).inputs[i]);
            }
            for(int i=0;i<dataSet.yn;i++){
                doubleProperties[dataSet.xn+2*i]=new SimpleDoubleProperty();
                doubleProperties[dataSet.xn+2*i].set(dataSet.oldtestGroups.get(n).outputs[i]);
            }
            for(int i=0;i<dataSet.yn;i++){
                doubleProperties[dataSet.xn+2*i+1]=new SimpleDoubleProperty();
                doubleProperties[dataSet.xn+2*i+1].set(dataSet.testPredicts.get(n)[i]);
            }
        }
    }
    class PredictData{
        public SimpleDoubleProperty[] doubleProperties;

        public PredictData(double[] inputs,double[] doutputs,double[] ooutputs) {
            this.doubleProperties= new SimpleDoubleProperty[inputs.length + 3 * doutputs.length];
            for(int i=0;i<inputs.length;i++){
                doubleProperties[i]=new SimpleDoubleProperty();
                doubleProperties[i].set(inputs[i]);
            }
            for(int i=0;i<doutputs.length;i++){
                doubleProperties[inputs.length+2*i]=new SimpleDoubleProperty();
                doubleProperties[inputs.length+2*i].set(doutputs[i]);
            }
            for(int i=0;i<doutputs.length;i++){
                doubleProperties[inputs.length+2*i+1]=new SimpleDoubleProperty();
                doubleProperties[inputs.length+2*i+1].set(ooutputs[i]);
            }
            for(int i=0;i<doutputs.length;i++){
                doubleProperties[inputs.length+2*doutputs.length+i]=new SimpleDoubleProperty();
                doubleProperties[inputs.length+2*doutputs.length+i].set(ooutputs[i]-doutputs[i]);
            }
        }
    }

    @FXML
    TextField dataCount;
    @FXML
    TextField predictDataCount;
    @FXML
    TextField inputCount;
    @FXML
    TextField outputCount;
    @FXML
    TextArea expressionText;
    @FXML
    TextArea displayText;

    ScriptEngineManager manager;
    ScriptEngine engine;

    @FXML
    void generateDataSet() throws IOException {
        int datacount= Integer.parseInt(dataCount.getText());
        int predictcount=Integer.parseInt(predictDataCount.getText());
        int inputcount=Integer.parseInt(inputCount.getText());
        int outputcount=Integer.parseInt(outputCount.getText());

        try {
            engine.eval("function uniform(min,max)\n" +
                    "{\n" +
                    "\treturn min+Math.random()*(max-min);\n" +
                    "}\n" +
                    "function randGaussian(m)\n" +
                    "{\n" +
                    "\tsum=0;\n" +
                    "\tfor(var i=0;i<m;i++){\n" +
                    "\t\tsum+=(Math.random()-1/2);\n" +
                    "\t}\n" +
                    "\treturn Math.sqrt(12/m)*sum;\n" +
                    "}\n" +
                    "function gaussian(m,a)\n" +
                    "{\n" +
                    "\treturn randGaussian(10000)*a+m;\n" +
                    "}");
//            double result=(double)engine.eval("gaussian(10,3)");
            DataSet dataSet=new DataSet(inputcount,outputcount);
            DecimalFormat df = new DecimalFormat("00000.00");
            for(int i=0;i<datacount;i++){
                engine.eval(expressionText.getText());

                double[] inputs=new double[inputcount];
                for(int j=0;j<inputcount;j++){
                    double x= (double) engine.get("x"+j);
                    inputs[j]=x;
                }
                double[] outputs=new double[outputcount];
                for(int j=0;j<outputcount;j++){
                    double y=(double)engine.get("y"+j);
                    outputs[j]=y;
                }
                DataGroup dg=new DataGroup(inputcount,outputcount);
                dg.inputs=inputs;
                dg.outputs=outputs;
                dataSet.trainGroups.add(dg);
            }

            DataSet predictdDataSet=new DataSet(inputcount,outputcount);
            for(int i=0;i<predictcount;i++){
                engine.eval(expressionText.getText());

                double[] inputs=new double[inputcount];
                for(int j=0;j<inputcount;j++){
                    double x= (double) engine.get("x"+j);
                    inputs[j]=x;
                }
                double[] outputs=new double[outputcount];
                for(int j=0;j<outputcount;j++){
                    double y=(double)engine.get("y"+j);
                    outputs[j]=y;
                }
                DataGroup dg=new DataGroup(inputcount,outputcount);
                dg.inputs=inputs;
                dg.outputs=outputs;
                predictdDataSet.trainGroups.add(dg);
            }

            displayText.setText("");
            String display="";
            for(int j=0;j<inputcount;j++){
                display+=("    x"+j+"\t\t");
            }
            for(int j=0;j<outputcount;j++){
                display+=("    y"+j+"\t\t");
            }
            display+="\n";
            for(int i=0;i<dataSet.trainGroups.size();i++){
                for(int j=0;j<dataSet.trainGroups.get(i).inputs.length;j++){
                    display+=(precossNumber(df.format(dataSet.trainGroups.get(i).inputs[j]))+"\t");
                }
                for(int j=0;j<dataSet.trainGroups.get(i).outputs.length;j++){
                    display+=(precossNumber(df.format(dataSet.trainGroups.get(i).outputs[j]))+"\t");
                }
                display+="\n";
            }
            displayText.setText(display);


//            File trainfile=new File("res/train.txt");//debug
//            File predictfile=new File("res/predict.txt");

            File trainfile=new File("res/train.txt");//for artifact
            File predictfile=new File("res/predict.txt");
            if(!trainfile.exists())
                trainfile.createNewFile();
            if(!predictfile.exists())
                predictfile.createNewFile();
            FileWriter fileWriter=new FileWriter(trainfile);
            BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);
            for(int i=0;i<dataSet.trainGroups.size();i++){
                for(int j=0;j<dataSet.trainGroups.get(i).inputs.length;j++){
                    bufferedWriter.write(new Double(dataSet.trainGroups.get(i).inputs[j]).toString());
                    bufferedWriter.write("\t");
                }
                for(int j=0;j<dataSet.trainGroups.get(i).outputs.length;j++){
                    bufferedWriter.write(new Double(dataSet.trainGroups.get(i).outputs[j]).toString());
                    bufferedWriter.write("\t");
                }
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
            fileWriter=new FileWriter(predictfile);
            bufferedWriter=new BufferedWriter(fileWriter);
            for(int i=0;i<predictdDataSet.trainGroups.size();i++){
                for(int j=0;j<predictdDataSet.trainGroups.get(i).inputs.length;j++){
                    bufferedWriter.write(new Double(predictdDataSet.trainGroups.get(i).inputs[j]).toString());
                    bufferedWriter.write("\t");
                }
                for(int j=0;j<dataSet.trainGroups.get(i).outputs.length;j++){
                    bufferedWriter.write(new Double(predictdDataSet.trainGroups.get(i).outputs[j]).toString());
                    bufferedWriter.write("\t");
                }
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
        } catch (ScriptException e) {
            displayText.setText(e.getMessage());
            e.printStackTrace();
        }

    }
    String precossNumber(String str){
        StringBuffer sb=new StringBuffer(str);
        int k=0;
        for(int i=0;i<str.length()-1;i++){
            if(sb.charAt(i)=='-')
                continue;
            if(sb.charAt(i)!='0'||(sb.charAt(i)=='0'&&sb.charAt(i+1)=='.')){
                break;
            }
            sb.setCharAt(i,' ');
            k++;
        }
        if(sb.charAt(0)=='-'){
            sb.setCharAt(0,' ');
            sb.setCharAt(k,'-');
        }
        return sb.toString();
    }
}
