import docx

from DbConst import TagType, FactType, TagValue


class DocxReader():
    line_no = 0
    full_line = ""
    line_type = 0

    #
    idFact = 3000000
    idFactTag = 3000000
    idItem = 3000000
    idItemTag = 3000000
    #
    file_out_Fact = None
    file_out_FactTag = None
    file_out_Item = None
    file_out_ItemTag = None

    #
    file_out_csv = None
    file_out_csv_bad = None
    file_out_csv_good = None

    def flishLine(self):
        self.full_line = self.stripLine(self.full_line)

        if len(self.full_line) != 0:
            self.validateLine(self.line_type, self.full_line)

            #
            self.file_out_csv.write(str(self.line_type) + "\t" + self.full_line + "\n")

            # Для анализа
            if self.line_type == 1 and self.full_line.count(",") >= 3:
                self.file_out_csv_bad.write(str(self.line_type) + "\t" + self.full_line + "\n")
            else:
                self.file_out_csv_good.write(str(self.line_type) + "\t" + self.full_line + "\n")

            # В базу
            self.full_line = self.full_line.lower()
            tab_pos = self.full_line.find("\t")
            if tab_pos != -1:
                text1 = self.full_line[0:tab_pos]
                text2 = self.full_line[tab_pos + 1:]
            else:
                text1 = self.full_line
                text2 = ""

            if self.line_type == 1:
                self.idItem = self.idItem + 1
                self.file_out_Item.write(str(self.idItem) + "\t" + text1 + "\n")
                self.idItemTag = self.idItemTag + 1
                self.file_out_ItemTag.write(str(self.idItemTag) + "\t" + str(self.idItem) + "\t" + TagType.word_lang + "\t" + TagValue.kaz + "\n")

                #
                self.idFact = self.idFact + 1
                self.file_out_Fact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + "\\N" + "\t" + FactType.spelling + "\t" + text1 + "\n")
                self.idFactTag = self.idFactTag + 1
                self.file_out_FactTag.write(str(self.idFactTag) + "\t" + str(self.idFact) + "\t" + TagType.word_lang + "\t" + TagValue.kaz + "\n")
                #
                self.idFact_ref = self.idFact

                #
                if len(text2) != 0:
                    self.idFact = self.idFact + 1
                    self.file_out_Fact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + "\\N" + "\t" + FactType.translate + "\t" + text2 + "\n")
                    self.idFactTag = self.idFactTag + 1
                    self.file_out_FactTag.write(str(self.idFactTag) + "\t" + str(self.idFact) + "\t" + TagType.translate_direction + "\t" + TagValue.kaz_rus + "\n")
                    self.idFactTag = self.idFactTag + 1
                    self.file_out_FactTag.write(str(self.idFactTag) + "\t" + str(self.idFact) + "\t" + TagType.dictionary + "\t" + TagValue.dictionary_full + "\n")
                    #
                    self.idFact_ref = self.idFact


            elif self.line_type == 2:
                text_example = text1 + "--" + text2
                #
                self.idFact = self.idFact + 1
                self.file_out_Fact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + str(self.idFact_ref) + "\t" + FactType.example + "\t" + text_example + "\n")

        #
        self.full_line = ""
        self.line_type = 0

    def validateLine(self, line_type, line):
        if line_type == 1 and not line[:1].isupper():
            print(line)
            self.file_out_csv.close()
            raise Exception("lineType == 1 and not text isupper, text: " + line)

        if line_type == 2 and line.find("\t") == -1:
            print(line)
            self.file_out_csv.close()
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

    def read_docx_with_indents(self, file_in, dir_out):
        self.file_out_Fact = open(dir_out + "/Fact.csv", "w", encoding="utf-8")
        self.file_out_FactTag = open(dir_out + "/FactTag.csv", "w", encoding="utf-8")
        self.file_out_Item = open(dir_out + "/Item.csv", "w", encoding="utf-8")
        self.file_out_ItemTag = open(dir_out + "/ItemTag.csv", "w", encoding="utf-8")

        self.file_out_csv = open(dir_out + "/out.csv", "w", encoding="utf-8")
        self.file_out_csv_bad = open(dir_out + "/out_bad.csv", "w", encoding="utf-8")
        self.file_out_csv_good = open(dir_out + "/out_good.csv", "w", encoding="utf-8")

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
                self.line_type = 1
                self.collectLine(text)

            elif indent >= 567 and indent <= 851:
                # пример использования слова
                self.flishLine()
                self.line_type = 2
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
        self.file_out_Fact.close()
        self.file_out_FactTag.close()
        self.file_out_Item.close()
        self.file_out_ItemTag.close()

        #
        self.file_out_csv.close()
        self.file_out_csv_good.close()
        self.file_out_csv_bad.close()


# Пример использования
reader = DocxReader()
file_in = "3875_sozdik.docx"
file_out = "3875_sozdik"
dir_out = "."
reader.read_docx_with_indents(file_in, dir_out)
