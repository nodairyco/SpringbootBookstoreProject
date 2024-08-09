First project using Springboot. 

# Bookstore
### Version 0.5
---
Dependencies:
- Lombok (easy creation of Objects)
- Spring Web (for communication via HTTP methods)
- Spring Security (Securing the project via [JSON web tokens](jwt.io))
- io.jsonwebtoken.* (all the methods for creating and decoding JWTs)
- Spring data Jpa (setting up schema for the DB)
- PostgreSQL driver (to connect to a postgres DB)
- H2 in memeroy database driver (for testing only *even though I haven't implemented any Integration tests yet*)
- Model Mapper (mapping entites to DTOs)
- *ill add more if needed*

---
The whole idea is that Users can login, buy books, create groups and share books with other people in a group.

So far I've implemented 70% of the program. 
Users can login, authenticate, safely delete accounts, and create groups.
In groups the role higherarchy goes as ``GROUP_ADMIN_`` > ``GROU_ELDER_`` > ``GROUP_MEMBER``. Admins can do anything they wish. Elders can only invite and remove members. Members can see the shared books and the members in a group.
I have implemented only mapping from BookDto to BookEntity for books.

---
btw u can do this in intellij
![image](https://github.com/user-attachments/assets/845bc319-591b-413e-8986-088ddc7b234e)
