package bpnn;

import bpnn.DataSet.DataSet;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zsh96 on 2016/10/1.
 */
public class BPNN {
    public static Random random=new Random(System.currentTimeMillis());

    Layer[] layers;
    int layerNumber;

    public double error;

    int maxIter;
    double minError;

    String netStructure;

    double speed,momentum;

    public DataSet dataSet;
    int dataNumber;

    public int xnumber,ynumber;

    public int nmaxtrainnumber=0;

    public ArrayList errorlist;
    public BPNN(int maxIter, double minError, String netStructure, double speed, double momentum) {
        this.maxIter = maxIter;
        this.minError = minError;
        this.netStructure = netStructure;
        this.speed = speed;
        this.momentum = momentum;

        this.errorlist=new ArrayList();

        error=Double.MAX_VALUE;

        divideStructure(netStructure);
    }

    void divideStructure(String netStructure){
        String[] ss= netStructure.split("-");
        layerNumber=ss.length;
        layers=new Layer[layerNumber];

        int iNumber= Integer.parseInt(ss[0]);
        layers[0]=new InputLayer(iNumber,0);

        for(int i=1;i<layerNumber;i++){
            int n=Integer.parseInt(ss[i]);
            layers[i]=new CalculateableLayer(n,i,layers[i-1].nodeNumber);
        }

        xnumber=iNumber;
        ynumber=Integer.parseInt(ss[layerNumber-1]);
    }

    public void train(DataSet dataSet) throws Exception {

        this.dataNumber=dataSet.groups.size();

        for(int iter=0;iter<maxIter;iter++){
            //--10.2-每组数据都进行数据前馈和误差反馈
            if(error<=minError)
                break;

            clearAverageError();
            error=0;
            for(int datacount=0;datacount<dataNumber;datacount++){
                double[] dX=dataSet.groups.get(datacount).inputs;
                for(int i=0;i<dX.length;i++){
                    InputNode node = (InputNode) layers[0].nodes[i];
                    node.x=dX[i];
                }
                _forward();
                error+=calculateSingleError(datacount);

                setOutputAverageError(datacount);
                backward();
            }
            error/=(dataNumber*2.0);

            errorlist.add(error);
            System.out.println(error);
            //然后刷新网络的结构
            refreshNet();

            nmaxtrainnumber++;
            //--10.2--

        }
    }
    public double[] predict(double[] X) throws Exception {
        int nX=layers[0].nodeNumber;
        int nY=layers[layerNumber-1].nodeNumber;
        if(X.length!=nX)
            return null;

        double[] Y=new double[nY];

        for(int i=0;i<X.length;i++){
            InputNode node = (InputNode) layers[0].nodes[i];
            node.x=X[i];
        }

        for(int layercount=1;layercount<layerNumber;layercount++){
            CalculateableLayer layer=(CalculateableLayer) layers[layercount];
            Layer lastlayer=layers[layercount-1];
            double[] stempX=lastlayer.getOutput();
            for(int nodecount=0;nodecount<layer.nodeNumber;nodecount++){
                CalculateableNode node= (CalculateableNode) layer.nodes[nodecount];
                node.activate(stempX);
            }
        }

        Layer outputlayer=layers[layerNumber-1];
        for(int i=0;i<outputlayer.nodeNumber;i++){
            Y[i]=outputlayer.nodes[i].x;
        }
        return Y;
    }

//    public void forward() throws Exception {
//        clearOutputAverageError();
//        error=0;
//        for(int datacount=0;datacount<dataNumber;datacount++){
//            //--
//            double[] dX=dataSet.groups.get(datacount).inputs;
//            for(int i=0;i<dX.length;i++){
//                InputNode node = (InputNode) layers[0].nodes[i];
//                node.x=dX[i];
//            }
//            // --
//
//            for(int layercount=1;layercount<layerNumber;layercount++){
//                CalculateableLayer layer=(CalculateableLayer) layers[layercount];
//                Layer lastlayer=layers[layercount-1];
//                double[] X=lastlayer.getOutput();
//                for(int nodecount=0;nodecount<layer.nodeNumber;nodecount++){
//                    CalculateableNode node= (CalculateableNode) layer.nodes[nodecount];
//                    node.activate(X);
//                }
//            }
//            error+=calculateSingleError(datacount);
//
//            //输出层每个节点的均差
//            setOutputAverageError(datacount);
//
//        }
//        error/=(dataNumber*2.0);//总平均误差
//        errorlist.add(error);
//
//        System.out.println(error);
//        Layer outputLayer=layers[layerNumber-1];
//        for(int nodeCount=0;nodeCount<outputLayer.nodeNumber;nodeCount++){
//            CalculateableNode node = (CalculateableNode) outputLayer.nodes[nodeCount];
//            node.averageError/=dataNumber;
//        }
//    }

    public void _forward() throws Exception {
        for(int layercount=1;layercount<layerNumber;layercount++){
            CalculateableLayer layer=(CalculateableLayer) layers[layercount];
            Layer lastlayer=layers[layercount-1];
            double[] X=lastlayer.getOutput();
            for(int nodecount=0;nodecount<layer.nodeNumber;nodecount++){
                CalculateableNode node= (CalculateableNode) layer.nodes[nodecount];
                node.activate(X);
            }

        }
    }

    double calculateSingleError(int dataCount){
        double sumError=0;
        Layer outputLayer=layers[layerNumber-1];
        for(int i=0;i<outputLayer.nodeNumber;i++){
            double dy=dataSet.groups.get(dataCount).outputs[i];
            double oy=outputLayer.nodes[i].x;
            sumError+=Math.pow(dy-oy,2.0);
        }
        return sumError;
    }

    void backward(){
        for(int layercount=layerNumber-2;layercount>0;layercount--){
            CalculateableLayer layer= (CalculateableLayer) layers[layercount];

            for(int nodecount=0;nodecount<layer.nodeNumber;nodecount++){
                //计算均差
                calculateHiddenAverageError(layercount,nodecount);
            }
        }

//        //修改W
//        for(int layercount=1;layercount<layerNumber;layercount++){
//            CalculateableLayer layer= (CalculateableLayer) layers[layercount];
//            Layer lastlayer= layers[layercount-1];
//            for(int nodecount=0;nodecount<layer.nodeNumber;nodecount++){
//                CalculateableNode node= (CalculateableNode) layer.nodes[nodecount];
//                for(int i=0;i<node.weightNumber;i++){
//                    node.incrementW[i]=speed*node.averageError*lastlayer.nodes[i].x+momentum*node.incrementW[i];
//                    node.W[i]+=node.incrementW[i];
//                }
//                node.incrementB=speed*node.averageError+momentum*node.incrementB;
//                node.b+=node.incrementB;
//            }
//        }

    }

    double calculateSingleOutputAverageError(int dataCount,int nodeNumber){
        Layer outputLayer=layers[layerNumber-1];
        double dy=dataSet.groups.get(dataCount).outputs[nodeNumber];
        CalculateableNode ynode= (CalculateableNode) outputLayer.nodes[nodeNumber];
        double oy=ynode.x;
        return (dy-oy)*ynode.derivative;
    }
    void clearAverageError(){
        for(int laycount=1;laycount<layerNumber;laycount++){
            Layer layer=layers[laycount];
            for(int nodecount=0;nodecount<layer.nodeNumber;nodecount++){
                CalculateableNode node= (CalculateableNode) layer.nodes[nodecount];
                node.averageError=0;
            }
        }

//        Layer outputLayer=layers[layerNumber-1];
//        for(int nodeCount=0;nodeCount<outputLayer.nodeNumber;nodeCount++){
//            CalculateableNode node = (CalculateableNode) outputLayer.nodes[nodeCount];
//            node.averageError=0;
//        }
    }
    void setOutputAverageError(int datacount){
        Layer outputLayer=layers[layerNumber-1];
        for(int nodeCount=0;nodeCount<outputLayer.nodeNumber;nodeCount++){
            CalculateableNode node = (CalculateableNode) outputLayer.nodes[nodeCount];
            node.averageError+=(calculateSingleOutputAverageError(datacount,nodeCount)/dataNumber);
        }
    }

    double calculateHiddenAverageError(int layercount,int nodecount){
        CalculateableLayer layer= (CalculateableLayer) layers[layercount];
        CalculateableLayer nextlayer= (CalculateableLayer) layers[layercount+1];

        CalculateableNode node= (CalculateableNode) layer.nodes[nodecount];

        double sum=0;
        for(int nnodec=0;nnodec<nextlayer.nodeNumber;nnodec++){
            CalculateableNode nnode= (CalculateableNode) nextlayer.nodes[nnodec];
            sum+=(nnode.averageError*nnode.W[nodecount]);
        }
        node.averageError+=sum*node.derivative/dataNumber;
        return node.averageError;
    }

    void refreshNet(){
        for(int layercount=1;layercount<layerNumber;layercount++){
            CalculateableLayer layer= (CalculateableLayer) layers[layercount];
            Layer lastlayer= layers[layercount-1];
            for(int nodecount=0;nodecount<layer.nodeNumber;nodecount++){
                CalculateableNode node= (CalculateableNode) layer.nodes[nodecount];
                for(int i=0;i<node.weightNumber;i++){
                    node.incrementW[i]=speed*node.averageError*lastlayer.nodes[i].x+momentum*node.incrementW[i];
                    node.W[i]+=node.incrementW[i];
                }
                node.incrementB=speed*node.averageError+momentum*node.incrementB;
                node.b+=node.incrementB;
            }
        }
    }
}