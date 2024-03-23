-- 创建 test_group 表
CREATE TABLE IF NOT EXISTS test_group
(
    id            BIGINT PRIMARY KEY,
    test_group_id CHAR(12) UNIQUE,
    create_time   TIMESTAMP DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')),
    update_time   TIMESTAMP DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))
);

-- 创建 standard_code 表
CREATE TABLE IF NOT EXISTS standard_code
(
    id            BIGINT PRIMARY KEY,
    code_id       VARCHAR(12) NOT NULL,
    test_group_id VARCHAR(12) NOT NULL,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (code_id)
);

-- 创建 sandbox_setting 表
CREATE TABLE IF NOT EXISTS sandbox_setting
(
    id          BIGINT PRIMARY KEY,
    base_url    VARCHAR(64) NOT NULL,
    name        VARCHAR(64),
    state       INT         NOT NULL,
    level       INT         NOT NULL,
    frequency   INT         NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (base_url)
);

-- 创建 sandbox_run 表
CREATE TABLE IF NOT EXISTS sandbox_run
(
    id          BIGINT PRIMARY KEY,
    file_id     VARCHAR(64) NOT NULL,
    base_url    VARCHAR(64) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (file_id)
);

-- 创建 setting 表
CREATE TABLE IF NOT EXISTS setting
(
    id          BIGINT PRIMARY KEY,
    key         VARCHAR(64)   NOT NULL,
    value       VARCHAR(8192) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (key)
);

-- 创建 system_metrics 表
CREATE TABLE IF NOT EXISTS system_metrics
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    SandboxSetting_id     BIGINT NOT NULL,
    cpu_core_usage        VARCHAR(255), -- 存储为 JSON 格式的字符串
    cpu_logical_cores     INT,
    cpu_physical_cores    INT,
    cpu_total_usage       DOUBLE,
    disk_read_kbps        DOUBLE,
    disk_write_kbps       DOUBLE,
    memory_total_mb       DOUBLE,
    memory_usage_percent  DOUBLE,
    memory_used_mb        DOUBLE,
    network_download_mbps DOUBLE,
    network_upload_mbps   DOUBLE,
    create_time           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建 language_config 表
CREATE TABLE IF NOT EXISTS language_config
(
    id          BIGINT PRIMARY KEY,
    server_id   BIGINT,
    language_id VARCHAR(64) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (server_id,language_id)
);

-- 创建 language_dictionary 表
CREATE TABLE IF NOT EXISTS language_dictionary
(
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    language            VARCHAR(255) NOT NULL,
    src_path            VARCHAR(255) NOT NULL,
    exe_path            VARCHAR(255) NOT NULL,
    compile_env         VARCHAR(255),
    compile_command     TEXT,
    compile_maxCpuTime  VARCHAR(255),
    compile_maxRealTime VARCHAR(255),
    compile_maxMemory   VARCHAR(255),
    run_env             VARCHAR(255),
    run_command         TEXT,
    UNIQUE (language)
);


-- 插入语言配置
INSERT INTO language_dictionary (id, language, src_path, exe_path, compile_env, compile_command, compile_maxCpuTime,
                                 compile_maxRealTime, compile_maxMemory, run_env, run_command)
VALUES (1, 'C', 'main.c', 'main', 'default',
        '/usr/bin/gcc -DONLINE_JUDGE -w -fmax-errors=1 -std=c11 {src_path} -lm -o {exe_path}', '5s', '10s', '256mb',
        'default', '/w/{exe_path}'),
       (2, 'C With O2', 'main.c', 'main', 'default',
        '/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=1 -std=c11 {src_path} -lm -o {exe_path}', '5s', '10s', '256mb',
        'default', '/w/{exe_path}'),
       (3, 'C++', 'main.cpp', 'main', 'default',
        '/usr/bin/g++ -DONLINE_JUDGE -w -fmax-errors=1 -std=c++14 {src_path} -lm -o {exe_path}', '10s', '20s', '512mb',
        'default', '/w/{exe_path}'),
       (4, 'C++ With O2', 'main.cpp', 'main', 'default',
        '/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=1 -std=c++14 {src_path} -lm -o {exe_path}', '10s', '20s',
        '512mb', 'default', '/w/{exe_path}'),
       (5, 'C++ 17', 'main.cpp', 'main', 'default',
        '/usr/bin/g++ -DONLINE_JUDGE -w -fmax-errors=1 -std=c++17 {src_path} -lm -o {exe_path}', '10s', '20s', '512mb',
        'default', '/w/{exe_path}'),
       (6, 'C++ 17 With O2', 'main.cpp', 'main', 'default',
        '/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=1 -std=c++17 {src_path} -lm -o {exe_path}', '10s', '20s',
        '512mb', 'default', '/w/{exe_path}'),
       (7, 'C++ 20', 'main.cpp', 'main', 'default',
        '/usr/bin/g++ -DONLINE_JUDGE -w -fmax-errors=1 -std=c++2a {src_path} -lm -o {exe_path}', '10s', '20s', '512mb',
        'default', '/w/{exe_path}'),
       (8, 'C++ 20 With O2', 'main.cpp', 'main', 'default',
        '/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=1 -std=c++2a {src_path} -lm -o {exe_path}', '10s', '20s',
        '512mb', 'default', '/w/{exe_path}'),
       (9, 'Java', 'Main.java', 'Main.jar', 'default',
        '/bin/bash -c "javac -encoding utf-8 {src_path} && jar -cvf {exe_path} *.class"', '10s', '20s', '512mb',
        'default', '/usr/bin/java -Dfile.encoding=UTF-8 -cp /w/{exe_path} Main'),
       (10, 'Python2', 'main.py', 'main.pyc', 'default', '/usr/bin/python -m py_compile /w/{src_path}', '5s', '10s',
        '256mb', 'default', '/usr/bin/python /w/{exe_path}'),
       (11, 'Python3', 'main.py', '__pycache__/main.cpython-37.pyc', 'python3',
        '/usr/bin/python3.7 -m py_compile /w/{src_path}', '5s', '10s', '256mb', 'python3',
        '/usr/bin/python3.7 /w/{exe_path}'),
       (12, 'PyPy2', 'main.py', '__pycache__/main.pypy-73.pyc', 'default', '/usr/bin/pypy -m py_compile /w/{src_path}',
        '5s', '10s', '256mb', 'default', '/usr/bin/pypy /w/{exe_path}'),
       (13, 'PyPy3', 'main.py', '__pycache__/main.pypy39.pyc', 'python3', '/usr/bin/pypy3 -m py_compile /w/{src_path}',
        '5s', '10s', '256mb', 'python3', '/usr/bin/pypy3 /w/{exe_path}'),
       (14, 'Golang', 'main.go', 'main', 'golang_compile', '/usr/bin/go build -o {exe_path} {src_path}', '5s', '10s',
        '512mb', 'golang_run', '/w/{exe_path}'),
       (15, 'C#', 'Main.cs', 'main', 'default', '/usr/bin/mcs -optimize+ -out:{exe_path} {src_path}', '5s', '10s',
        '512mb', 'default', '/usr/bin/mono /w/{exe_path}'),
       (16, 'PHP', 'main.php', 'main.php', '', '', '', '', '', 'default', '/usr/bin/php /w/{exe_path}'),
       (17, 'JavaScript Node', 'main.js', 'main.js', '', '', '', '', '', 'default', '/usr/bin/node /w/{exe_path}'),
       (18, 'JavaScript V8', 'main.js', 'main.js', '', '', '', '', '', 'default', '/usr/bin/jsv8/d8 /w/{exe_path}'),
       (19, 'Ruby', 'main.rb', 'main.rb', '', '', '', '', '', 'default', '/usr/bin/ruby /w/{exe_path}'),
       (20, 'Rust', 'Main.cs', 'main', 'default', '/usr/bin/rustc -O -o {exe_path} {src_path}', '5s', '10s', '512mb',
        'default', '/w/{exe_path}');


-- 创建 language_install表
CREATE TABLE IF NOT EXISTS language_install
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    language     VARCHAR(255) NOT NULL,
    package_name VARCHAR(255) NOT NULL,
    UNIQUE (language)
);

insert into language_install (id, language, package_name)
VALUES (1, 'C', 'gcc'),
       (2, 'C++', 'g++'),
       (3, 'Python3', 'python3'),
       (4, 'Pascal', 'fpc'),
       (5, 'Java', 'openjdk-17-jdk'),
       (6, 'C#', 'mono-mcs'),
       (7, 'PHP', 'php-cli'),
       (8, 'JavaScript', 'nodejs'),
       (9, 'TypeScript', 'node-typescript'),
       (10, 'Ruby', 'ruby'),
       (11, 'Rust', 'rustc'),
       (12, 'Golang', 'golang-go');

-- 创建 language_install_logs表
CREATE TABLE IF NOT EXISTS language_install_logs
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    server_id   BIGINT         NOT NULL,
    message     VARCHAR(16384) NOT NULL,
    level       INT            NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
