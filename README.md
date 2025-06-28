# 🐾 Cadê Meu Pet?

**Cadê Meu Pet?** é um aplicativo Android desenvolvido para ajudar pessoas a encontrarem seus animais de estimação desaparecidos. Ele conecta membros da comunidade que perderam seus pets com aqueles que os encontraram, promovendo uma rede de apoio e empatia.

---

## 📱 Funcionalidades

- 📋 Cadastro e login de usuários com Firebase Authentication  
- 🐶 Registro de pets desaparecidos com:
  - Foto
  - Nome
  - Descrição
  - Data do desaparecimento
  - Status (Desaparecido ou Encontrado)
- 📍 Notificação ao dono quando alguém encontrar um pet
- 🔍 Filtro por status dos animais (Todos, Desaparecidos, Encontrados)
- 🧑‍💻 Tela de edição de perfil, telefone e endereço separado por campos

---

## 🧪 Tecnologias utilizadas

- Java (Android SDK)
- Firebase Firestore (Realtime Database)
- Firebase Authentication
- Material Design
- ConstraintLayout e RecyclerView
- Envio de email (notificação ao dono)

---

## 📸 Capturas de Tela



---

## 🚀 Como rodar o projeto localmente

1. Clone o repositório:

```bash
git clone https://github.com/seu-usuario/cade-meu-pet.git
```

2. Abra o projeto no Android Studio

3. Configure o Firebase:

- Adicione seu google-services.json à pasta app/

- Habilite Firestore e Authentication (Email/Senha)

- Compile e execute em um dispositivo ou emulador

---

## 📈 Futuras melhorias

- Integração com localização GPS
- Comentários/comunicação entre usuários
- Integração com redes sociais
- Denúncia de fake reports
- Dark Mode adaptativo

