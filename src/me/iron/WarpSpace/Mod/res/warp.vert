#version 120

varying vec4 ambient;
varying vec3 vert;
varying vec3 vertPos;
varying vec3 normal,halfVector;
varying float mainRadius;
void main() {

    /* first transform the normal into eye space and normalize the result */
    normal = normalize(gl_NormalMatrix * gl_Vertex.xyz); //This is 'cheating' because the model is a sphere.
    //Normally (ha ha) you would multiply gl_NormalMatrix * gl_Normal, but the normals coming out of this model are rotated somehow wrong.
    //We can just ignore them and use the vertex position because on a perfect sphere, normals should always be facing straight out, along
    //the vector that points from the center of the model out (or in this case in) through the vertex in question


    /* Normalize the halfVector to pass it to the fragment shader */
    halfVector = normalize(gl_LightSource[0].halfVector.xyz);

    /* Compute the ambient and globalAmbient terms (removed diffuse) */
    ambient = vec4(0,0,0,0);
    ambient += gl_LightModel.ambient * gl_FrontMaterial.ambient;
    ambient = vec4(.6,.6,.6,1.0);
    gl_TexCoord[0] = gl_MultiTexCoord0;

    gl_FrontColor = gl_Color;

    vert = vec3(gl_ModelViewMatrix * gl_Vertex);
    vertPos = gl_Vertex.xyz;

    mainRadius = gl_Vertex.w;

    gl_Position = gl_ModelViewProjectionMatrix * vec4(gl_Vertex.xyz, 1.0);
}
