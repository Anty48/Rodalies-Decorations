package rodalies.client;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import rodalies.RailSignWallBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modelo horneado de una señal de pared ({@link RailSignWallBlock}) desplazado hacia su soporte segun
 * la propiedad {@code offset}. Envuelve el modelo normal y traslada todos sus quads; asi no hace falta
 * un JSON por cada distancia. Se instala por estado en {@code ClientSetup#onModifyBakingResult}.
 *
 * Al desplazarse, las caras dejan de estar en el borde de la celda, asi que todo se devuelve como
 * quads sin cullface (side == null) para que nunca se oculten por el bloque vecino.
 */
public class OffsetSignModel extends BakedModelWrapper<BakedModel> {

    private final IQuadTransformer transformer;
    private final Map<RenderType, List<BakedQuad>> cache = new ConcurrentHashMap<>();
    @Nullable
    private volatile List<BakedQuad> untypedCache;

    /** @param dx,dz traslacion en bloques (direccion opuesta al facing × offset). */
    public OffsetSignModel(BakedModel original, float dx, float dz) {
        super(original);
        this.transformer = QuadTransformers.applying(new Transformation(new Matrix4f().translation(dx, 0, dz)));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData data, @Nullable RenderType renderType) {
        if (side != null) {
            return List.of();
        }
        if (renderType == null) {
            List<BakedQuad> quads = untypedCache;
            if (quads == null) {
                quads = shifted(state, rand, data, null);
                untypedCache = quads;
            }
            return quads;
        }
        return cache.computeIfAbsent(renderType, rt -> shifted(state, rand, data, rt));
    }

    private List<BakedQuad> shifted(@Nullable BlockState state, RandomSource rand, ModelData data,
                                    @Nullable RenderType renderType) {
        List<BakedQuad> out = new ArrayList<>();
        for (Direction d : Direction.values()) {
            out.addAll(originalModel.getQuads(state, d, rand, data, renderType));
        }
        out.addAll(originalModel.getQuads(state, null, rand, data, renderType));
        return transformer.process(out);
    }
}
