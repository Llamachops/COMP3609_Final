import java.awt.Rectangle;

public interface Enemy {
    void takeDamage(float damage);
    boolean isAlive();
    Rectangle getHitbox();
    boolean isDead();
}