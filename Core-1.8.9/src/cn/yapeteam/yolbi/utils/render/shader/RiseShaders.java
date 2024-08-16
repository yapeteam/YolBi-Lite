package cn.yapeteam.yolbi.utils.render.shader;

import cn.yapeteam.yolbi.utils.render.shader.base.RiseShader;
import cn.yapeteam.yolbi.utils.render.shader.impl.*;

public interface RiseShaders {
    AlphaShader ALPHA_SHADER = new AlphaShader();
    RiseShader BLOOM_SHADER = new BloomShader();
    RiseShader UI_BLOOM_SHADER = new BloomShader();
    RiseShader UI_POST_BLOOM_SHADER = new BloomShader();
    BlurShader GAUSSIAN_BLUR_SHADER = new BlurShader();
    BlurShader UI_GAUSSIAN_BLUR_SHADER = new BlurShader();

    RiseShader OUTLINE_SHADER = new OutlineShader();
    RQShader RQ_SHADER = new RQShader();
    RGQShader RGQ_SHADER = new RGQShader();
    ROQShader ROQ_SHADER = new ROQShader();
    ROGQShader ROGQ_SHADER = new ROGQShader();
    RiseShader MAIN_MENU_SHADER = new MainMenuBackgroundShader();
    RiseShader BAW_SHADER = new BAWShader();
    RGQTestShader RGQ_SHADER_TEST = new RGQTestShader();

    RTriGQShader R_TRI_GQ_SHADER = new RTriGQShader();
}
