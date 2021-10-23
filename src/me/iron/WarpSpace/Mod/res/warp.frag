#version 120

varying vec4 diffuse,ambient;
varying vec3 normal,lightDir,halfVector;

uniform float time;
varying float mainRadius;


float rand(vec2 n) {
    return fract(cos(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);
}

float noise(vec2 n) {
    const vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n), f = smoothstep(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

float fbm(vec2 n) {
    float total = 0.0, amplitude = 1.0;
    for (int i = 0; i < 4; i++) {
        total += noise(n) * amplitude;
        n += n;
        amplitude *= 0.5;
    }
    return total;
}
vec3 create(float ycoord){
    float totalLength = gl_TexCoord[0].z;

    const vec3 c1 = vec3(166.0/255.0, 244.0/255.0, 255.0/255.0);
    const vec3 c2 = vec3(173.0/255.0, 100.0/255.0, 121.4/255.0);
    const vec3 c3 = vec3(1.6, 1.6, 1.6);
    const vec3 c4 = vec3(164.0/255.0, 33.0/255.0, 214.4/255.0);
    const vec3 c5 = vec3(0.6);
    const vec3 c6 = vec3(0.9);
    vec2 p = vec2(gl_TexCoord[0].x*(totalLength*0.85), ycoord*mainRadius*6.0);// * 5.0 / 1024.0;
    float q = fbm(p - time * 0.1);
    vec2 r = vec2(fbm(p + q + time * 1.0 - p.x - p.y), fbm(p + q - time * 1.0));
    vec3 c = mix(c1, c2, fbm(p + r)) + mix(c3, c4, r.x) - mix(c5, c6, r.y);
    return c;
}
void main()
{





    vec3 n,halfV,viewV,ldir;
    float NdotL,NdotHV;
    vec4 color = ambient;

    float t = time * 0.2;
    float glow = 0.6 - abs(((t - float(int(t)))*1.5-0.25) - gl_TexCoord[0].x);


    color -= 0.3;
    color.rg *= max(1.0,  pow(0.66+glow,16));

    color *= gl_Color;

    /* a fragment shader can't write a verying variable, hence we need
    a new variable to store the normalized interpolated normal */
    n = normalize(normal);

    /* compute the dot product between normal and ldir */
    NdotL = max(dot(n,lightDir),0.0);

    if (NdotL > 0.0) {
        halfV = normalize(halfVector)*0.97;
        NdotHV = max(dot(n,halfV),0.0);
        color += gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess);
        color += diffuse*NdotL*0.45;
    }

    float m = gl_TexCoord[0].y - 0.5;
    float d = 0.5 - abs(m);
    vec3 c;
    if(d < 0.2){
        //fixes seam from noon looping texture on y tex coord
        vec3 c1 = create(gl_TexCoord[0].y);
        vec3 c2 = create(0.9+d);

        c = mix(c2, c1, d*5.0);
    }else{
        c = create(gl_TexCoord[0].y);
    }

    gl_FragColor = vec4(c.rgb*color.rgb, 1.0);//vec4(c*color.rgb, 1.0);
}
