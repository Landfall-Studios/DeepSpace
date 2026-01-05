package world.landfall.deepspace.render.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import foundry.veil.api.client.color.Color;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.LinkedList;

public class Sphere implements DeepSpaceRenderable {
    private final LinkedList<Triangle> triangles;
    public Sphere(double radius, int longitudeLines, int latitudeLines) {
        triangles = new LinkedList<Triangle>();

        int numVertices = latitudeLines * (longitudeLines + 1) + 2;
        Vec3[] positions = new Vec3[numVertices];
        Vec2[] texcoords = new Vec2[numVertices];

        positions[0] = new Vec3(0, radius, 0);
        texcoords[0] = new Vec2(0, 1);

        positions[numVertices-1] = new Vec3(0, -radius, 0);
        texcoords[numVertices-1] = new Vec2(0, 0);

        float latitudeSpacing = 1.0f / (latitudeLines + 1.0f);
        float longitudeSpacing = 1.0f / (longitudeLines);

        int v = 1;
        for (int latitude = 0; latitude < latitudeLines; latitude++)
            for (int longitude = 0; longitude <= longitudeLines; longitude++) {

                float theta = longitude * longitudeSpacing * 2.0f * (float)Math.PI;
                float phi = (1.0f - (latitude + 1) * latitudeSpacing - 0.5f) * (float)Math.PI;
                texcoords[v] = new Vec2(
                        longitude * longitudeSpacing,
//                        1.0f - (latitude + 1) * latitudeSpacing
                        (phi) / (1 * (float)Math.PI) + .5f
                );

                float c = (float)Math.cos(phi);

                positions[v] = new Vec3(
                        c * (float)Math.cos(theta),
                        (float)Math.sin(phi),
                        c * (float)Math.sin(theta)
                ).scale(radius);
                v++;
            }
        // Caps
        Vec3 top = positions[0];
        Vec3 bottom = positions[numVertices-1];
        for (int longitude = 0; longitude < longitudeLines; longitude++) {
            Vec3 pos1 = positions[longitude+1];
            Vec3 pos2 = positions[longitude+2];
            Vec2 tex1 = texcoords[longitude+1];
            Vec2 tex2 = texcoords[longitude+2];
            addTriangle(pos2, top, pos1, tex2, tex2.add(tex1).scale(.5f), tex1);
            pos1 = positions[numVertices-(longitude+1)-1];
            pos2 = positions[numVertices-(longitude+2)-1];
            tex1 = texcoords[numVertices-(longitude+1)-1];
            tex2 = texcoords[numVertices-(longitude+2)-1];
            addTriangle(pos2, bottom, pos1, tex2, tex2.add(tex1).scale(.5f), tex1);

        }


        // Body Triangles
        for (int latitude = 1; latitude < latitudeLines; latitude++) {
            for (int longitude = 0; longitude < longitudeLines; longitude++) {
                int correctedLongitudeLines = longitudeLines+1;
                Vec3 pos1 = positions[(latitude - 1) * correctedLongitudeLines + longitude + 1];
                Vec3 pos2 = positions[(latitude - 1) * correctedLongitudeLines + longitude + 2];
                Vec3 pos3 = positions[latitude * correctedLongitudeLines + longitude + 1];
                Vec3 pos4 = positions[latitude * correctedLongitudeLines + longitude + 2];
                Vec2 tex2 = texcoords[(latitude - 1) * correctedLongitudeLines + longitude + 2];
                Vec2 tex1 = texcoords[(latitude - 1) * correctedLongitudeLines + longitude + 1];
                Vec2 tex3 = texcoords[latitude * correctedLongitudeLines + longitude + 1];
                Vec2 tex4 = texcoords[latitude * correctedLongitudeLines + longitude + 2];
                addTriangle(pos2, pos1, pos3, tex2, tex1, tex3);
                addTriangle(pos2, pos3, pos4, tex2, tex3, tex4);
            }
        }
    }
    private void addTriangle(Vec3 x, Vec3 y, Vec3 z, Vec2 xUV, Vec2 yUV, Vec2 zUV) {
        addTriangle(x.toVector3f(), y.toVector3f(), z.toVector3f(), new Vector2f(xUV.x, xUV.y), new Vector2f(yUV.x, yUV.y), new Vector2f(zUV.x, zUV.y));
    }
    private void addTriangle(Vector3f x, Vector3f y, Vector3f z, Vector2f xUV, Vector2f yUV, Vector2f zUV) {

        triangles.add(new Triangle(
                new Vector3f[] {x, y, z},
                new Vector2f[] {xUV, yUV, zUV}
        ));
    }

    private Vector3f vertexAtAngles(double theta, double phi, double radius) {
        double x = radius * Math.sin(phi) * Math.cos(theta);
        double y = radius * Math.sin(phi) * Math.sin(theta);
        double z = radius * Math.cos(phi);
        return new Vector3f((float)x, (float)y, (float)z);
    }
    @Override
    public void render(PoseStack stack, VertexConsumer consumer, Vector3fc dimensions, Quaternionf rotation) {
        for (var triangle : triangles) {
            for (int i = 0; i < 3; i++) {
                var oldVertex = triangle.vertexes[i];
                var vertex = new Vector3f(oldVertex.x, oldVertex.y, oldVertex.z);
                vertex.rotate(rotation);
                var UV = triangle.UV[i];
                var normal = new Vector3f(vertex).mul(1);
                consumer.addVertex(vertex.x+dimensions.x(), vertex.y+dimensions.y(), vertex.z+dimensions.z(),
                        Color.WHITE.argb(),
                        UV.x, UV.y,
                        0, 255, normal.x, normal.y, normal.z
                );
            }
        }
    }
    private static class Triangle {
        public final Vector3f[] vertexes;
        public final Vector2f[] UV;
        public Triangle(Vector3f[] _vertexes, Vector2f[] _UV) {
            vertexes = _vertexes;
            UV = _UV;
        }
    }
}
