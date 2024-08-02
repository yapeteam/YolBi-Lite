#include <iostream>
#include <string>
#include <iomanip>
#include <algorithm>
#include <sstream>
#include <curl/curl.h>
#include <json/json.h>
#include <openssl/rsa.h>
#include <openssl/des.h>
#include <openssl/pem.h>
#include <openssl/err.h>
#include <openssl/sha.h>
#include <openssl/evp.h>
#include <openssl/rand.h>

size_t WriteCallback(void *contents, size_t size, size_t nmemb, void *userp)
{
    ((std::string *)userp)->append((char *)contents, size * nmemb);
    return size * nmemb;
}

std::string http_post(const std::string &url, const std::string &data, CURLcode &res)
{
    CURL *curl;
    std::string readBuffer;

    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();
    if (curl)
    {
        struct curl_slist *headers = NULL;
        headers = curl_slist_append(headers, "Content-Type: application/json");
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data.c_str());
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 10);
        res = curl_easy_perform(curl);
        curl_easy_cleanup(curl);
    }
    curl_global_cleanup();

    return readBuffer;
}

std::string http_get(const std::string &url, CURLcode &res)
{
    CURL *curl;
    std::string readBuffer;

    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();
    if (curl)
    {
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 10);
        res = curl_easy_perform(curl);
        curl_easy_cleanup(curl);
    }
    curl_global_cleanup();

    return readBuffer;
}

#define KEY_LENGTH 2048 // 密钥长度

/*
制造密钥对：私钥和公钥
**/
void GenerateRSAKey(std::string &out_pub_key, std::string &out_pri_key)
{
    size_t pri_len = 0;      // 私钥长度
    size_t pub_len = 0;      // 公钥长度
    char *pri_key = nullptr; // 私钥
    char *pub_key = nullptr; // 公钥

    // 生成密钥对
    RSA *keypair = RSA_generate_key(KEY_LENGTH, RSA_3, NULL, NULL);

    BIO *pri = BIO_new(BIO_s_mem());
    BIO *pub = BIO_new(BIO_s_mem());

    // 生成私钥
    PEM_write_bio_RSAPrivateKey(pri, keypair, NULL, NULL, 0, NULL, NULL);
    // 注意------生成第1种格式的公钥
    // PEM_write_bio_RSAPublicKey(pub, keypair);
    // 注意------生成第2种格式的公钥(此处代码中使用这种)
    PEM_write_bio_RSA_PUBKEY(pub, keypair);

    // 获取长度
    pri_len = BIO_pending(pri);
    pub_len = BIO_pending(pub);

    // 密钥对读取到字符串
    pri_key = (char *)malloc(pri_len + 1);
    pub_key = (char *)malloc(pub_len + 1);

    BIO_read(pri, pri_key, pri_len);
    BIO_read(pub, pub_key, pub_len);

    pri_key[pri_len] = '\0';
    pub_key[pub_len] = '\0';

    out_pub_key = pub_key;
    out_pri_key = pri_key;

    // 释放内存
    RSA_free(keypair);
    BIO_free_all(pub);
    BIO_free_all(pri);

    free(pri_key);
    free(pub_key);
}

std::string base64Encode(const std::vector<unsigned char> &input)
{
    BIO *bio = BIO_new(BIO_s_mem());
    BIO *b64 = BIO_new(BIO_f_base64());
    bio = BIO_push(b64, bio);

    BIO_write(bio, input.data(), input.size());
    BIO_flush(bio);

    BUF_MEM *bufferPtr;
    BIO_get_mem_ptr(bio, &bufferPtr);
    std::string output(bufferPtr->data, bufferPtr->length - 1);

    BIO_free_all(bio);
    return output;
}

std::string hexToString(const std::string &hex)
{
    std::string result;
    for (size_t i = 0; i < hex.length(); i += 2)
    {
        std::string byte = hex.substr(i, 2);
        char chr = static_cast<char>(strtol(byte.c_str(), nullptr, 16));
        result.push_back(chr);
    }
    return result;
}

std::string stringToHex(const std::string &input)
{
    static const char hex_digits[] = "0123456789ABCDEF";
    std::string output;
    output.reserve(input.length() * 2);
    for (unsigned char c : input)
    {
        output.push_back(hex_digits[c >> 4]);
        output.push_back(hex_digits[c & 0x0F]);
    }
    return output;
}

// 加密并Base64编码
std::string encrypt(const std::string &plaintext, const std::string &public_key_pem)
{
    BIO *bio = BIO_new_mem_buf(public_key_pem.data(), -1);
    if (!bio)
    {
        std::cerr << "BIO_new_mem_buf failed." << std::endl;
        return "";
    }

    RSA *rsa = PEM_read_bio_RSA_PUBKEY(bio, nullptr, nullptr, nullptr);
    if (!rsa)
    {
        std::cerr << "PEM_read_bio_RSA_PUBKEY failed." << std::endl;
        BIO_free(bio);
        return "";
    }

    std::vector<unsigned char> encrypted_data(RSA_size(rsa));
    int flen = RSA_public_encrypt(plaintext.size(), (unsigned char *)plaintext.c_str(), encrypted_data.data(), rsa, RSA_PKCS1_OAEP_PADDING);
    RSA_free(rsa);
    BIO_free(bio);

    if (flen < 0)
    {
        std::cerr << "Encryption failed." << std::endl;
        return "";
    }

    std::vector<unsigned char> encrypted(encrypted_data.begin(), encrypted_data.begin() + flen);
    return base64Encode(encrypted);
}

// Base64解码并解密
std::vector<unsigned char> base64Decode(const std::string &encoded)
{
    BIO *bio = BIO_new_mem_buf(encoded.data(), encoded.size());
    BIO *b64 = BIO_new(BIO_f_base64());
    bio = BIO_push(b64, bio);

    std::vector<unsigned char> decoded(encoded.size());
    int decodedLength = BIO_read(bio, decoded.data(), encoded.size());
    decoded.resize(decodedLength);

    BIO_free_all(bio);
    return decoded;
}

// 解密函数
std::string decryptString(const std::vector<unsigned char> &encrypted, const std::string &privateKey)
{
    RSA *rsa = nullptr;
    BIO *keybio = BIO_new_mem_buf((void *)privateKey.c_str(), -1);
    if (keybio == nullptr)
    {
        return nullptr;
    }

    rsa = PEM_read_bio_RSAPrivateKey(keybio, &rsa, nullptr, nullptr);
    if (rsa == nullptr)
    {
        BIO_free_all(keybio);
        return nullptr;
    }

    std::vector<unsigned char> decrypted(RSA_size(rsa));
    int result = RSA_private_decrypt(encrypted.size(), encrypted.data(), decrypted.data(), rsa, RSA_PKCS1_OAEP_PADDING);
    if (result == -1)
    {
        RSA_free(rsa);
        BIO_free_all(keybio);
        return nullptr;
    }

    RSA_free(rsa);
    BIO_free_all(keybio);
    return std::string(decrypted.begin(), decrypted.begin() + result);
}

// 解密并Base64解码
std::string decrypt(const std::string &encrypted_text, const std::string &private_key_pem)
{
    const std::vector<unsigned char> encrypted(encrypted_text.begin(), encrypted_text.end());
    return decryptString(encrypted, private_key_pem);
}

std::string generate_rsa_key_pair(std::string &public_key)
{
    RSA *rsa = RSA_generate_key(2048, RSA_F4, NULL, NULL);
    BIO *priv = BIO_new(BIO_s_mem());
    BIO *pub = BIO_new(BIO_s_mem());

    PEM_write_bio_RSAPrivateKey(priv, rsa, NULL, NULL, 0, NULL, NULL);
    PEM_write_bio_RSAPublicKey(pub, rsa);

    char *priv_key_cstr;
    long priv_len = BIO_get_mem_data(priv, &priv_key_cstr);
    std::string private_key(priv_key_cstr, priv_len);

    char *pub_key_cstr;
    long pub_len = BIO_get_mem_data(pub, &pub_key_cstr);
    public_key = std::string(pub_key_cstr, pub_len);

    BIO_free_all(priv);
    BIO_free_all(pub);
    RSA_free(rsa);

    return private_key;
}

// 生成随机盐
std::string generateSalt(size_t length)
{
    unsigned char salt[length];
    RAND_bytes(salt, length);
    std::stringstream ss;
    for (size_t i = 0; i < length; ++i)
    {
        ss << std::hex << std::setw(2) << std::setfill('0') << (int)salt[i];
    }
    return ss.str();
}

std::string hash256(const std::string &data)
{
    unsigned char hash[8];
    SHA256((unsigned char *)data.c_str(), data.size(), hash);
    std::stringstream ss;
    for (size_t i = 0; i < 8; ++i)
    {
        ss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }
    return ss.str();
}

// 生成带盐的哈希值
std::string hashWithSalt(const std::string &data, const std::string &salt)
{
    std::string saltedData = data + salt;
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256((unsigned char *)saltedData.c_str(), saltedData.size(), hash);
    std::stringstream ss;
    for (size_t i = 0; i < SHA256_DIGEST_LENGTH; ++i)
    {
        ss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }
    return ss.str();
}

bool verifyHash(const std::string &data, const std::string &salt, const std::string &expectedHash)
{
    return hashWithSalt(data, salt) == expectedHash;
}

std::vector<std::string> split(const std::string &str, const std::string &delim)
{
    std::vector<std::string> res;
    if ("" == str)
        return res;
    // 先将要切割的字符串从string类型转换为char*类型
    char *strs = new char[str.length() + 1]; // 不要忘了
    strcpy(strs, str.c_str());

    char *d = new char[delim.length() + 1];
    strcpy(d, delim.c_str());

    char *p = strtok(strs, d);
    while (p)
    {
        std::string s = p; // 分割得到的字符串转换为string类型
        res.push_back(s);  // 存入结果数组
        p = strtok(NULL, d);
    }

    return res;
}

bool string_to_bool(const std::string &str)
{
    std::string lower_str = str;
    std::transform(lower_str.begin(), lower_str.end(), lower_str.begin(), ::tolower);
    return lower_str == "true" || lower_str == "1";
}

#include <array>
#include <windows.h>
#include <intrin.h>
#include <iphlpapi.h>
#pragma comment(lib, "iphlpapi.lib")

std::string get_cpu_id()
{
    std::array<int, 4> cpui;
    __cpuid(cpui.data(), 0);
    std::ostringstream result;
    result << cpui[0] << cpui[1] << cpui[2] << cpui[3];
    return result.str();
}

std::string get_mac_address()
{
    IP_ADAPTER_INFO AdapterInfo[16];
    DWORD dwBufLen = sizeof(AdapterInfo);
    DWORD dwStatus = GetAdaptersInfo(AdapterInfo, &dwBufLen);
    if (dwStatus != ERROR_SUCCESS)
        return "";

    PIP_ADAPTER_INFO pAdapterInfo = AdapterInfo;
    std::ostringstream result;
    result << std::hex;
    for (int i = 0; i < 6; ++i)
    {
        result << (int)pAdapterInfo->Address[i];
        if (i < 5)
            result << ":";
    }
    return result.str();
}

std::string get_machine_identifier()
{
    std::string cpu_id = get_cpu_id();
    std::string mac_address = get_mac_address();
    return cpu_id + "-" + mac_address;
}

void setMsg(JNIEnv *env, jclass cls, std::string msg)
{
    jfieldID msgFieldID = env->GetStaticFieldID(cls, "msg", "Ljava/lang/String;");
    jstring jMsg = env->NewStringUTF(msg.c_str());
    env->SetStaticObjectField(cls, msgFieldID, jMsg);
}

std::string sha256(const std::string &str)
{
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, str.c_str(), str.size());
    SHA256_Final(hash, &sha256);

    std::stringstream ss;
    for (int i = 0; i < SHA256_DIGEST_LENGTH; ++i)
    {
        ss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }
    return ss.str();
}

void activateUser(const std::string &username, const std::string &cdk, JNIEnv *env, jclass cls)
{
    std::string server_url = "http://111.173.106.116:5000";
    CURL *curl;
    CURLcode res;
    std::string readBuffer;

    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();

    if (curl)
    {
        std::string url = server_url + "/activate";
        Json::Value jsonData;
        jsonData["username"] = username;
        jsonData["cdk"] = cdk;

        Json::StreamWriterBuilder writer;
        std::string jsonStr = Json::writeString(writer, jsonData);

        struct curl_slist *headers = NULL;
        headers = curl_slist_append(headers, "Content-Type: application/json");

        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, jsonStr.c_str());

        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);

        res = curl_easy_perform(curl);
        if (res != CURLE_OK)
        {
            setMsg(env, cls, curl_easy_strerror(res));
            fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
        }
        else
        {
            // read json

            Json::Value root;
            Json::CharReaderBuilder reader;
            std::string errs;
            std::istringstream s(readBuffer);
            if (Json::parseFromStream(reader, s, &root, &errs))
            {
                if (root.isMember("error"))
                    setMsg(env, cls, root["error"].asString());

                if (root.isMember("message"))
                    setMsg(env, cls, root["message"].asString());
                else
                    setMsg(env, cls, readBuffer);
            }
            curl_easy_cleanup(curl);
        }

        curl_global_cleanup();
        return;
    }
}

bool verifyUser(const std::string &username, const std::string &password, JNIEnv *env, jclass cls)
{
    std::string server_url = "http://111.173.106.116:5000";
    CURLcode res;
    std::string public_key_response = http_get(server_url + "/get_public_key", res);
    if (res != CURLE_OK)
    {
        setMsg(env, cls, curl_easy_strerror(res));
        return false;
    }

    Json::Value root;
    Json::CharReaderBuilder reader;
    std::string errs;
    std::istringstream s(public_key_response);
    std::string public_server_key;
    if (Json::parseFromStream(reader, s, &root, &errs))
    {
        if (root.isMember("error"))
        {
            setMsg(env, cls, root["error"].asString());
            return false;
        }
        public_server_key = hexToString(root["000"].asString());
    }

    std::string public_client_key;
    std::string private_client_key = generate_rsa_key_pair(public_client_key);

    std::string salt = generateSalt(32);

    std::string data_to_encrypt = username +
                                  "||" + password + "||" + "1337" + "||" + hash256(get_machine_identifier()) + "||" +
                                  std::to_string(time(NULL)) + "||" + salt;

    std::string C2Stoken = encrypt(data_to_encrypt, public_server_key);

    Json::Value payload;
    payload["000"] = C2Stoken;                            // 加密后的token
    payload["111"] = salt;                                // 盐
    payload["222"] = hashWithSalt(data_to_encrypt, salt); // 哈希值
    payload["333"] = stringToHex(public_client_key);      // PublicClientKey

    Json::StreamWriterBuilder writer;
    std::string request_data = Json::writeString(writer, payload);

    std::string response = http_post(server_url + "/validate", request_data, res);
    if (res != CURLE_OK)
    {
        std::cerr << curl_easy_strerror(res) << std::endl;
        setMsg(env, cls, "Failed to verify user");
        return false;
    }

    Json::Value response_root;
    std::istringstream response_s(response);
    if (Json::parseFromStream(reader, response_s, &response_root, &errs))
    {
        if (response_root.isMember("error"))
        {
            std::cerr << "Error: " << response_root["error"].asString() << std::endl;
            setMsg(env, cls, response_root["error"].asString());
            return false;
        }
        std::string S2Ctoken_encrypted = response_root["000"].asString();
        std::string salt_received = response_root["111"].asString();
        std::string hash_received = response_root["222"].asString();
        std::string decrypted_token = decrypt(hexToString(S2Ctoken_encrypted), private_client_key);

        if (!verifyHash(decrypted_token, salt_received, hash_received))
            ((void (*)())0x1337)();

        std::vector<std::string> token_parts = split(decrypted_token, "||");
        if (token_parts.size() < 5)
            ((void (*)())0x1337)();

        std::string playerID_received = token_parts[0];
        std::string Version_received = token_parts[1];
        std::string HWID_received = token_parts[2];
        std::string CurrentTime_received = token_parts[3];
        bool isActivated_received = string_to_bool(token_parts[4]);

        int current_time = time(NULL);
        int time_diff = current_time - std::stoi(CurrentTime_received);
        if (time_diff > 10)
            ((void (*)())0x1337)();

        if (!isActivated_received)
            setMsg(env, cls, "User not activated");
        return isActivated_received;
    }
}
