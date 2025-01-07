package frc.robot.subsystems;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class IntakeSubsystem {
    private static IntakeSubsystem instance = new IntakeSubsystem();
    private SparkMax intakeMotor;
    private boolean intakeEnabled;
    private double speed = -0.5;
    private boolean alreadyStopped;
    private boolean algaeDetected = false;

    private IntakeSubsystem() {
        intakeMotor = new SparkMax(15, SparkLowLevel.MotorType.kBrushless);

        SparkMaxConfig config = new SparkMaxConfig();

        config.inverted(true);

        intakeMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        intakeEnabled = false;
        alreadyStopped = false;
        SmartDashboard.putBoolean("Intake Enabled", intakeEnabled);
    }

    public static IntakeSubsystem getInstance(){
        return instance;
    }

    public void periodic(){
        if(intakeEnabled)
            intakeMotor.set(speed);
        else{
            intakeMotor.set(0);
        }

        SmartDashboard.putBoolean("Intake Enabled", intakeEnabled);
        SmartDashboard.putNumber("IntakeSpeed", speed);
        SmartDashboard.putBoolean("AlreadyStopped", alreadyStopped);
    }

   
    public void startIntake(){
        intakeEnabled = true;
        System.out.println("Intake Enabled");
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

    public void stopintake(){
        intakeEnabled = false;
        System.out.println("Intake Disabled");
    }

    public double getSpeed(){
        return speed;
    }
}