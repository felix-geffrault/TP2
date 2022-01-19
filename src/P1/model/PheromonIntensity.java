package P1.model;

public class PheromonIntensity extends Thread{
    private Environnement env;
    private double value;
    private double r = 0.8;
    private int[] coords;

    public PheromonIntensity(double value, int[] coords) {
        this.coords = coords;
        this.value = value;
        this.start();
    }

    @Override
    public void run() {
        while(value > 0.1){
            try{
                value = value * r;
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        env.pheromonEvapored(this);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int[] getCoords() {
        return coords;
    }
}
