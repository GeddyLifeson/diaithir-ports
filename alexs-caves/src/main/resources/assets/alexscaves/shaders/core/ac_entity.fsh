#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

#ifdef DISSOLVE
uniform sampler2D DissolveMaskSampler;
#endif

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#ifdef PER_FACE_LIGHTING
in vec4 vertexPerFaceColorBack;
in vec4 vertexPerFaceColorFront;
#else
in vec4 vertexColor;
#endif

#ifndef EMISSIVE
in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
in vec4 overlayColor;
#endif

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif

#ifdef PER_FACE_LIGHTING
    vec4 faceVertexColor = gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack;
#else
    vec4 faceVertexColor = vertexColor;
#endif

#ifdef DISSOLVE
    if (faceVertexColor.a < texture(DissolveMaskSampler, texCoord0).a) {
        discard;
    }
    // The dissolve effect entirely replaces translucency
    faceVertexColor.a = 1.0;
#endif

    color *= faceVertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif

#if defined(IRRADIATED) || defined(BLUE_IRRADIATED)
    // pulsing radiation glow drawn over the model (upstream drew this into a blurred post target)
    float pulse = sin(GameTime * 2000.0) + 1.0;
#ifdef IRRADIATED
    color = vec4(0.15, pulse * 0.15 + 0.65, 0.0, color.a * 0.5);
#else
    color = vec4(0.0, pulse * 0.15 + 0.85, pulse * 0.15 + 0.85, color.a * 0.5);
#endif
    fragColor = color;
    return;
#endif
#ifdef PURPLE_WITCH
    float witchPulse = (sin(GameTime * 5000.0) + 1.0) * 0.5;
    vec3 witch = vec3(witchPulse * 0.15 + 0.85, 0.0, witchPulse * 0.15 + 0.85);
    fragColor = vec4(witch, color.a * 0.6);
    return;
#endif
#ifdef RED_GHOST
    float brightest = max(color.r, max(color.g, color.b));
    color = vec4(brightest * 0.9, brightest * 0.3, brightest * 0.05, color.a);
#endif
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
