-- phpMyAdmin SQL Dump
-- version 5.0.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Erstellungszeit: 03. Dez 2020 um 19:41
-- Server-Version: 10.4.14-MariaDB
-- PHP-Version: 7.4.11

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `falldetection`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `fall`
--

CREATE TABLE `fall` (
  `id` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `arduinoID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `fullname` text NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` password NOT NULL,
  `email` varchar(300) NOT NULL,
  `arduinoID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Daten für Tabelle `user`
--


INSERT INTO `user` (`id`, `fullname`, `username`, `password`, `email`, `arduinoID`) VALUES
(4, 'test', 'test', 'test', 'test@gmx.de', 1),
(5, 'bla', 'bla', 'bla', 'bla@gmx.de', 2),
(9, 'Bernd Waldenmaier', 'Bernd', 'Bernd', 'test@gmx.de', 3);

INSERT INTO `fall` (`id`, `date`, `arduinoID`) VALUES
(1, '2018-12-11', '2'),
(2, '2006-10-10', '2'),
(3, '2000-11-08', '2');



--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `fall`
--
ALTER TABLE `fall`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `fall`
--
ALTER TABLE `fall`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT für Tabelle `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `fall`
--
/*ALTER TABLE `fall`
  ADD CONSTRAINT `arduinoID` FOREIGN KEY (`arduinoID`) REFERENCES `user` (`arduinoID`);
COMMIT;*/

GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;


/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

