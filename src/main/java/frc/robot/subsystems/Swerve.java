package frc.robot.subsystems;

import frc.robot.SwerveModule;
import frc.robot.Constants;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import com.ctre.phoenix6.configs.Pigeon2Configuration;
import com.ctre.phoenix6.hardware.Pigeon2;

public class Swerve implements Subsystem{
    public SwerveDrivePoseEstimator swervePoseEstimator;
    public SwerveModule[] mSwerveMods;
    public Pigeon2 gyro;

    private final double accelerationTime = 0.3;
    private double speedMultiplier = 1;
    private double desiredSpeed = Constants.Swerve.maxSpeed * speedMultiplier;

    private double linearAcceleration = desiredSpeed / accelerationTime;

    private SlewRateLimiter m_xSlewRateLimiter = new SlewRateLimiter(linearAcceleration, -linearAcceleration, 0);
    private SlewRateLimiter m_ySlewRateLimiter = new SlewRateLimiter(linearAcceleration, -linearAcceleration, 0);

    private PIDController xController = new PIDController(0.6, 0, 0);
    private PIDController yController = new PIDController(0.6, 0, 0);
    private PIDController thetaController = new PIDController(0.035, 0, 0);
    private PIDController thetaController2 = new PIDController(0.0002, 0, 0);

    private boolean positionReached = false;

    public Swerve() {
        
        gyro = new Pigeon2(Constants.Swerve.pigeonID);
        gyro.getConfigurator().apply(new Pigeon2Configuration());
        gyro.setYaw(0);
        xController.setTolerance(0.2);
        yController.setTolerance(0.2);
        thetaController.setTolerance(3);
        thetaController2.setTolerance(3);
        thetaController.enableContinuousInput(0, 360);
        thetaController2.enableContinuousInput(0, 360);
        
        mSwerveMods = new SwerveModule[] {
            new SwerveModule(0, Constants.Swerve.Mod0.constants),
            new SwerveModule(1, Constants.Swerve.Mod1.constants),
            new SwerveModule(2, Constants.Swerve.Mod2.constants),
            new SwerveModule(3, Constants.Swerve.Mod3.constants)
        };

        swervePoseEstimator = new SwerveDrivePoseEstimator(Constants.Swerve.swerveKinematics, getGyroYaw(), getModulePositions(), new Pose2d());
    }

    public void setDriveOffsets(){
        mSwerveMods[0].setAngleOffset();
        mSwerveMods[1].setAngleOffset();
        mSwerveMods[2].setAngleOffset();
        mSwerveMods[3].setAngleOffset();
    }

    public void drive(Translation2d translation, double rotation, boolean fieldRelative, boolean isOpenLoop) {
        double xSpeed = m_xSlewRateLimiter.calculate(translation.getX() * speedMultiplier);
        double ySpeed = m_ySlewRateLimiter.calculate(translation.getY() * speedMultiplier);
        SmartDashboard.putNumber("xTarget", translation.getX());
        SmartDashboard.putNumber("yTarget", translation.getX());
        SmartDashboard.putNumber("RotationTarget", rotation);
        SwerveModuleState[] swerveModuleStates =
            Constants.Swerve.swerveKinematics.toSwerveModuleStates(
                fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
                                    xSpeed, 
                                    ySpeed, 
                                    -rotation, 
                                    getHeading()
                                )
                                : new ChassisSpeeds(
                                    xSpeed, 
                                    ySpeed, 
                                    -rotation
                                )
            );
        SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, desiredSpeed);

        for(SwerveModule mod : mSwerveMods){
            mod.setDesiredState(swerveModuleStates[mod.moduleNumber], isOpenLoop);
        }
    }
           
    public SwerveModuleState[] getModuleStates(){
        SwerveModuleState[] states = new SwerveModuleState[4];
        for(SwerveModule mod : mSwerveMods){
            states[mod.moduleNumber] = mod.getState();
        }
        return states;
    }

    public SwerveModulePosition[] getModulePositions(){
        SwerveModulePosition[] positions = new SwerveModulePosition[4];
        for(SwerveModule mod : mSwerveMods){
            positions[mod.moduleNumber] = mod.getPosition();
        }
        return positions;
    }

    public Pose2d getPose() {
        return swervePoseEstimator.getEstimatedPosition();
    }

    public ChassisSpeeds getRobotRelativeSpeeds(){
        return new ChassisSpeeds();
    }

    public void driveRobotRelative(ChassisSpeeds speeds) {
        Translation2d translation = new Translation2d(speeds.vxMetersPerSecond, speeds.vyMetersPerSecond);
        double rotation = speeds.omegaRadiansPerSecond;
        drive(translation, rotation, false, false);
    }

    public void resetPose(Pose2d pose) {
        swervePoseEstimator.resetPosition(getGyroYaw(), getModulePositions(), pose);
    }

    public Rotation2d getHeading(){
        return getPose().getRotation();
    }

    public void setHeading(Rotation2d heading){
        swervePoseEstimator.resetPosition(getGyroYaw(), getModulePositions(), new Pose2d(getPose().getTranslation(), heading));
    }

    public void zeroGyro() {
        swervePoseEstimator.resetPosition(getGyroYaw(), getModulePositions(), new Pose2d(getPose().getTranslation(), new Rotation2d()));
    }

    public Rotation2d getGyroYaw() {
        return Rotation2d.fromDegrees(gyro.getYaw().getValueAsDouble());
    }

    public void resetModulesToAbsolute(){
        for(SwerveModule mod : mSwerveMods){
            mod.resetToAbsolute();
        }
    }

    public void setSprint() {
        speedMultiplier = 1.3;
    }

    public void setCrawl() {
        speedMultiplier = 0.4375;
    }

    public void setBase() {
        speedMultiplier = 1;
    }

    public void setLocationReached(){
        positionReached = true;
    }

    public boolean isLocationReached(){
        return positionReached;
    }

    public void periodicValues(){
        swervePoseEstimator.update(getGyroYaw(), getModulePositions());
        for(SwerveModule mod : mSwerveMods){
            SmartDashboard.putNumber("Mod " + mod.moduleNumber + "CANcoder", mod.getCANcoder().getDegrees());
            SmartDashboard.putNumber("Mod " + mod.moduleNumber + "Angle", mod.getPosition().angle.getDegrees());
            SmartDashboard.putNumber("Mod " + mod.moduleNumber + "Velocity", mod.getState().speedMetersPerSecond);    
        }
    }

    public void teleopSwerve(DoubleSupplier translationSup, DoubleSupplier strafeSup, DoubleSupplier rotationSup, BooleanSupplier robotCentricSup) {
        double translationVal = MathUtil.applyDeadband(translationSup.getAsDouble(), Constants.stickDeadband);
        double strafeVal = MathUtil.applyDeadband(strafeSup.getAsDouble(), Constants.stickDeadband);
        double rotationVal = MathUtil.applyDeadband(rotationSup.getAsDouble(), Constants.stickDeadband);
        SmartDashboard.putNumber("RotationTarget",rotationVal);

        drive(
            new Translation2d(translationVal, strafeVal).times(Constants.Swerve.maxSpeed), 
            rotationVal * Constants.Swerve.maxAngularVelocity, 
            !robotCentricSup.getAsBoolean(), 
            false /* KEEP FALSE */
        );
    }

    public void periodic(DoubleSupplier translationSup, DoubleSupplier strafeSup, DoubleSupplier rotationSup, BooleanSupplier robotCentricSup) {
        teleopSwerve(translationSup, strafeSup, rotationSup, robotCentricSup);
        SmartDashboard.putNumber("X Pose", getPose().getX());
        SmartDashboard.putNumber("Y Pose", getPose().getY());
        SmartDashboard.putNumber("Drive Angle", getPose().getRotation().getDegrees());
    }
}