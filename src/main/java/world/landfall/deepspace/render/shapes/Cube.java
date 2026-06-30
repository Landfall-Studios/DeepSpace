package world.landfall.deepspace.render.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import foundry.veil.api.client.color.Color;
import org.joml.*;
import org.slf4j.Logger;

import java.lang.Math;
import java.util.LinkedList;

public class Cube implements DeepSpaceRenderable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private LinkedList<Triangle> TRIANGLES = new LinkedList<>();
    public final Vector3f center;
    private final boolean weirdNormals;
    private final boolean unwrapped;
    public final float radius;

    private final boolean[][] unwrappedMap = {
            {false, true, false, false},
            {true , true, true , true},
            {false, true, false, false},
    };


    public Cube(Vector3f _corner1, Vector3f _corner2, float scale, boolean weirdNormals, boolean unwrapped) {
        this.weirdNormals = weirdNormals;
        this.unwrapped = unwrapped;
        var corner1 = new Vector3f(
                _corner1.x,
                _corner1.y,
                _corner1.z
        );
        var corner2 = new Vector3f(
                _corner2.x,
                _corner2.y,
                _corner2.z
        );
        //corner1.mul(2);
        //corner2.mul(2);
        Vector3f center = new Vector3f(corner1).add(corner2).div(2);
        this.center = center;
        Vector3f diff = new Vector3f(corner1).sub(corner2).div(2);
        this.radius = Math.abs(diff.x);
        Quaternionf[] rotations = new Quaternionf[] {
                new Quaternionf().rotateLocalX(-(float)Math.PI/2),
                new Quaternionf(),
                new Quaternionf().rotateLocalY((float)Math.PI/2),
                new Quaternionf().rotateLocalY((float)Math.PI),
                new Quaternionf().rotateLocalY((float)Math.PI*1.5f),
                new Quaternionf().rotateLocalX((float)Math.PI/2),
        };
        diff.mul(scale);



        int faceind = 0;
        for (var x : rotations) {
            int xInd = -1, yInd = -1;
            int counter = 0;
            for (int i = 0; i < unwrappedMap.length; i++) {
                for (int j = 0; j < unwrappedMap[i].length; j++) {
                    if (unwrappedMap[i][j]) {
                        if (counter++ == faceind) {
                            xInd = i;
                            yInd = j;
                        }
                    }
                }
            }



            float[][] UVs = unwrapped && xInd > -1 ?
                    new float[][] {
                            {
                                    xInd * .25f, xInd * .25f + .25f
                            },
                            {
                                    yInd * .25f, yInd * .25f + .25f
                            },
                    }:
                    new float[][] {{0, 1}, {0, 1}};
            Vector3f[] vertexes = new Vector3f[] {
                    new Vector3f(diff.x, diff.y, diff.z),
                    new Vector3f(diff.x, -diff.y, diff.z),
                    new Vector3f(-diff.x, diff.y, diff.z),
                    new Vector3f(diff.x, -diff.y, diff.z),
                    new Vector3f(-diff.x, diff.y, diff.z),
                    new Vector3f(-diff.x, -diff.y, diff.z)
            };
            var triangle1 = new Triangle(
                    new Vector3f[] {
                            vertexes[0].rotate(x).add(center),
                            vertexes[1].rotate(x).add(center),
                            vertexes[2].rotate(x).add(center)
                    },
                    new Vector2f[] {
                            new Vector2f(UVs[0][0], UVs[1][0]),
                            new Vector2f(UVs[0][0], UVs[1][1]),
                            new Vector2f(UVs[0][1], UVs[1][0])
                    },
                    new Vector3f[] {
                            new Vector3f(0, 0, -1).rotate(x),
                            new Vector3f(0, 0, -1).rotate(x),
                            new Vector3f(0, 0, -1).rotate(x)
                    }
            );
            var triangle2 = new Triangle(
                    new Vector3f[] {
                            vertexes[4].rotate(x).add(center),
                            vertexes[3].rotate(x).add(center),
                            vertexes[5].rotate(x).add(center)
                    },
                    new Vector2f[] {
                            new Vector2f(UVs[0][1], UVs[1][0]),
                            new Vector2f(UVs[0][0], UVs[1][1]),
                            new Vector2f(UVs[0][1], UVs[1][1])
                    },
                    new Vector3f[] {
                            new Vector3f(0, 0, -1).rotate(x),
                            new Vector3f(0, 0, -1).rotate(x),
                            new Vector3f(0, 0, -1).rotate(x)
                }
            );
            TRIANGLES.add(triangle1);
            TRIANGLES.add(triangle2);
            faceind++;
        }
    }
    public Cube(Vector3f _corner1, Vector3f _corner2, float scale, boolean weirdNormals) {
        this(_corner1, _corner2, scale, weirdNormals, false);


    }
    @Override
    public void render(PoseStack stack, VertexConsumer consumer, Vector3fc dimensions, Quaternionf rotation) {
        for (var triangle : TRIANGLES) {
            for (int i = 0; i < 3; i++) {
                var oldVertex = triangle.vertexes[i];
                var vertex = new Vector3f(oldVertex.x, oldVertex.y, oldVertex.z);
                vertex.add(dimensions);
                vertex.rotate(rotation);

                var UV = triangle.UV[i];
                Vector3f normal;
                if (weirdNormals)
                    normal = new Vector3f(vertex).sub(center).normalize();
                else
                    normal = triangle.normals[i];
                consumer.addVertex(vertex.x, vertex.y, vertex.z,
                        Color.WHITE.argb(),
                        UV.x, UV.y,
                        0, 255, normal.x, normal.y, normal.z
                );
            }
        }
    }

}
