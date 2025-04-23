import JSEncrypt from 'jsencrypt';
import { publicKeyApi } from '@/api/auth'

function RSAUtil() {
    
    const encryptPassword = async (password: any) => {
        // 1. 获取公钥
        let publicKey = "";
        await publicKeyApi().then((res: any) => {
            if (res.code === 200) {
                publicKey = res.result;
            }
        });
    
        // 2. 初始化加密器
        const encryptor = new JSEncrypt();
        encryptor.setPublicKey(publicKey);
    
        // 3. 加密并返回 Base64 结果
        const encrypted = encryptor.encrypt(password);
        if (!encrypted) {
            throw new Error('加密失败，请检查公钥格式');
        }
        return encrypted;
    }

    return {
        encryptPassword
    }
}

export default RSAUtil