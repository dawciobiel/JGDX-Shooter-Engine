#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_lightmap;
uniform vec4 u_ambientColor;

void main() {
    vec4 diffuse = texture2D(u_texture, v_texCoords);
    vec4 light = texture2D(u_lightmap, v_texCoords);

    // Ambient: dark, low intensity
    // Light: additive from the lightmap
    vec3 ambient = u_ambientColor.rgb * u_ambientColor.a;
    vec3 intensity = ambient + light.rgb;

    // Result: Base texture color multiplied by (ambient + dynamic light)
    vec3 finalColor = diffuse.rgb * intensity;

    gl_FragColor = v_color * vec4(finalColor, diffuse.a);
}
