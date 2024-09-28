import docx


class DocxReader():
    line_no = 0
    full_line = ""
    lineType = 0

    file_out = None
    file_out_bad = None
    file_out_good = None

    def flishLine(self):
        self.full_line = self.stripLine(self.full_line)

        if len(self.full_line) != 0:
            self.validateLine(self.lineType, self.full_line)

            #
            self.file_out.write(str(self.lineType) + "\t" + self.full_line + "\n")

            # Для анализа
            if self.lineType == 1 and self.full_line.count(",") >= 3:
                self.file_out_bad.write(str(self.lineType) + "\t" + self.full_line + "\n")
            else:
                self.file_out_good.write(str(self.lineType) + "\t" + self.full_line + "\n")

        #
        self.full_line = ""
        self.lineType = 0

    def validateLine(self, lineType, line):
        if lineType == 1 and not line[:1].isupper():
            print(line)
            self.file_out.close()
            raise Exception("lineType == 1 and not text isupper, text: " + line)

        if lineType == 2 and line.find("\t") == -1:
            print(line)
            self.file_out.close()
            raise Exception("lineType == 2 and line.find(tab), text: " + line)

    def stripLine(self, line):
        line = line.replace("\t\t", "\t")
        line = line.replace("\t\t", "\t")
        line = line.replace("\t\t", "\t")
        line = line.replace("\t\t", "\t")
        line = line.replace("\t\t", "\t")
        line = line.replace(" ", " ")
        line = line.replace(" ", " ")
        line = line.replace(" ", " ")
        line = line.replace(" ", " ")
        line = line.replace(" ", " ")
        line = line.replace(",,", ",")
        line = line.replace(",,", ",")
        line = line.replace(",,", ",")
        line = line.replace(",,", ",")
        line = line.replace(",,", ",")
        line = line.strip()
        #
        tab_pos = line.find("\t")
        if tab_pos != -1:
            if line.find("притеснение, упр") != -1:
                pass

            # поделим по первой табуляции
            text1 = line[0:tab_pos]
            text2 = line[tab_pos + 1:]
            # Уберем остальные табуляции
            text2 = text2.replace("\t", " ")
            # Уберем слова, начинающиеся на 4 ("4жақа" и т.п.)
            words = text2.split()
            textX = ""
            for word in words:
                if word[:1] != "4":
                    textX = textX + " " + word
            text2 = textX.strip()
            # Уберем начальные 8 ("8едва, 8еле, 8если")
            text2 = text2.replace("8", ",")
            # Уберем повторяющиеся фрагменты
            text2 = text2.replace(", ", ",")
            text2 = text2.replace(" ,", ",")
            text2 = text2.replace(",,", ",")
            words = text2.split(",")
            textX = ","
            for word in words:
                wordF = "," + word + ","
                if textX.find(wordF) == -1:
                    textX = textX + word + ","
            text2 = textX.strip(",")
            # Соберем обратно
            text2 = text2.replace(",", ", ")
            line = text1 + "\t" + text2

        #
        return line

    def collectLine(self, text):
        self.full_line = self.full_line + "\t" + text

    def read_docx_with_indents(self, file_in, file_out):
        self.file_out = open(file_out + ".csv", "w", encoding="utf-8")
        self.file_out_bad = open(file_out + "_bad.csv", "w", encoding="utf-8")
        self.file_out_good = open(file_out + "_good.csv", "w", encoding="utf-8")

        # Открываем документ
        doc = docx.Document(file_in)

        for para in doc.paragraphs:
            # Получаем текст параграфа
            text = para.text

            if text.find("немногий, немного, слегка, чуточку, чуть") != -1:
                pass

            # Получаем отступы (в дюймах, их нужно умножить на 72, чтобы получить в пунктах)
            left_indent = para.paragraph_format.left_indent
            first_line_indent = para.paragraph_format.first_line_indent

            # Преобразуем отступы в удобный формат
            left_indent_twips = left_indent.twips if left_indent else 0
            first_line_indent_twips = first_line_indent.twips if first_line_indent else 0

            #
            indent = left_indent_twips + first_line_indent_twips

            # print(f"Параграф: {text}")
            # print(text)
            # print(f"Левый отступ: {left_indent_pt} пунктов")
            # print(f"Правый отступ: {right_indent_pt} пунктов")
            # print(f"Отступ первой строки: {first_line_indent_twips} twips")
            # print("-" * 40)

            # Если строка ничинается с одной табуляции - считаем, что это пример
            if indent == 0 and text[0:1] == "\t" and text[1:2] != "\t":
                indent = 600
                text = text[1:]

            # Если строка ничинается с пяти табуляций - считаем, что это перенос
            if indent == 0 and text[0:5] == "\t\t\t\t\t":
                indent = 4000
                text = text[5:]
            elif indent != 0 and text[0:4] == "\t\t\t\t":
                indent = 4000
                text = text[4:]

            if indent == 0:
                # слово
                self.flishLine()
                self.lineType = 1
                self.collectLine(text)

            elif indent >= 567 and indent <= 851:
                # пример использования слова
                self.flishLine()
                self.lineType = 2
                self.collectLine(text)

            elif indent >= 3510 and indent <= 4962:
                # перенос с прошлой строки
                self.collectLine(text)

            else:
                print(str(self.line_no) + ": " + text)
                raise Exception("bad indent: " + str(indent))

            #
            print(text)
            self.line_no = self.line_no + 1

        # Последний раз
        self.flishLine()

        #
        self.file_out.close()
        self.file_out_bad.close()


# Пример использования
reader = DocxReader()
file_in = "3875_sozdik.docx"
file_out = "3875_sozdik"
reader.read_docx_with_indents(file_in, file_out)
