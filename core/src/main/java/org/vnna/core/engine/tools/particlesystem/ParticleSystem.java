package org.vnna.core.engine.tools.particlesystem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.media_manager.media.CMediaAnimation;
import org.vnna.core.engine.media_manager.media.CMediaArray;
import org.vnna.core.engine.media_manager.media.CMediaImage;
import org.vnna.core.engine.tools.listthreadpool.LThreadPool;
import org.vnna.core.engine.tools.listthreadpool.LThreadPoolUpdater;
import org.vnna.core.engine.tools.listthreadpool.ThreadPoolAlgorithm;
import org.vnna.core.engine.tools.particlesystem.particle.Particle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;

/*
 * Particle System for Managing Classes that Extend Particle
 */
public class ParticleSystem<T extends Particle> {

    private ArrayList<T> particles;

    private ArrayDeque<T> deleteQueue;

    private MediaManager mediaManager;

    private int particleLimit;


    public ParticleSystem(MediaManager mediaManager) {
        this(mediaManager, Integer.MAX_VALUE);
    }

    public ParticleSystem(MediaManager mediaManager, int particleLimit) {
        this.mediaManager = mediaManager;
        this.particles = new ArrayList<>();
        this.deleteQueue = new ArrayDeque<>();
        this.particleLimit = particleLimit;
    }
    public void setParticleLimit(int particleLimit) {
        this.particleLimit = particleLimit;
    }

    public void update() {
        if (particles.size() == 0) return;
        for(int i=0;i<particles.size();i++){
            T particle = particles.get(i);
            if(!particle.update(this, particle, i)){
                deleteQueue.add(particle);
            }
        }
        // remove sorted indexes in reverse order
        T particle;
        while ((particle = deleteQueue.poll()) != null) {
            particles.remove(particle);
        }
    }


    public void forEveryParticle(Consumer<T> consumer) {
        for (int i = 0; i < particles.size(); i++) {
            consumer.accept(particles.get(i));
        }
        return;
    }

    public void removeAllParticles() {
        particles.clear();
        deleteQueue.clear();
    }

    public int getParticleCount() {
        return this.particles.size();
    }

    public void addParticle(T particle) {
        if (canAddParticle()) {
            this.particles.add(particle);
        }
    }


    public boolean canAddParticle() {
        return this.particles.size() < particleLimit;
    }

    public int canAddParticleCount() {
        return particleLimit - this.particles.size();
    }

    public void render(SpriteBatch batch) {
        render(batch, 0);
    }

    public void render(SpriteBatch batch, float animation_timer) {
        if (particles.size() == 0) return;

        for (T particle : this.particles) {
            if (!particle.visible) {
                continue;
            }
            batch.setColor(particle.r, particle.g, particle.b, particle.a);
            switch (particle.type) {
                case TEXT -> {
                    if (particle.text != null && particle.font != null) {
                        mediaManager.drawCMediaFont(batch, particle.font, particle.x, particle.y, particle.text);
                    }
                }
                case IMAGE -> {
                    mediaManager.drawCMediaImageScale(batch, (CMediaImage) particle.appearance, particle.x, particle.y, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY, particle.rotation);
                }
                case ARRAY -> {
                    mediaManager.drawCMediaArrayScale(batch, (CMediaArray) particle.appearance, particle.x, particle.y, particle.array_index, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY, particle.rotation);
                }
                case ANIMATION -> {
                    mediaManager.drawCMediaAnimationScale(batch, (CMediaAnimation) particle.appearance, particle.x, particle.y, animation_timer, particle.origin_x, particle.origin_y, particle.scaleX, particle.scaleY);
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    public void shutdown() {
        this.deleteQueue.clear();
        this.particles.clear();
    }
}
