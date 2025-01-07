package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel;
import edu.wpi.first.wpilibj.DigitalInput;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class IntakeSubsystem {
    private static IntakeSubsystem instance = new IntakeSubsystem();
    private SparkMax intakeMotor;
    private boolean intakeEnabled;
    private double speed = 0;
    private double pickUpSpeed = -0.5;
    private double holdSpeed = -0.07;
    private DigitalInput intakeSensor;
    private boolean alreadyStopped;

    private IntakeSubsystem() {
        intakeMotor = new SparkMax(15, SparkLowLevel.MotorType.kBrushless);
        
        SparkMaxConfig config = new SparkMaxConfig();
        config.idleMode(SparkBaseConfig.IdleMode.kBrake);
        intakeSensor = new DigitalInput(1);
        config.inverted(true);
        config.smartCurrentLimit(20);
        intakeMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        alreadyStopped = false;
        SmartDashboard.putBoolean("Intake Enabled", intakeEnabled);
    }

    public static IntakeSubsystem getInstance(){
        return instance;
    }

    public void periodic(){

        if(algaeAcquired()){
            holdIntake();
        }

        intakeMotor.set(speed);

        SmartDashboard.putBoolean("Intake Enabled", intakeEnabled);
        SmartDashboard.putNumber("IntakeSpeed", speed);
        SmartDashboard.putBoolean("AlreadyStopped", alreadyStopped);
    }

    public boolean algaeAcquired(){
        return !intakeSensor.get();
    }

   
    public void startIntake(){
        speed = pickUpSpeed;
        System.out.println("Intake Enabled");
    }

    public void holdIntake(){
        speed = holdSpeed;
        System.out.println("Intake Hold");
    }

    public void stopIntake(){
        speed = 0;
        System.out.println("Intake Disabled");
    }

    // public void toggleIntake(){
    //     if (intakeEnabled){
    //         intakeEnabled = false;
    //     }else{
    //         intakeEnabled = true;
    //     }
    // }

    public void startIntakeReverse(){
        speed = 1;
        intakeEnabled = true;
    }



    public double getSpeed(){
        return speed;
    }
}